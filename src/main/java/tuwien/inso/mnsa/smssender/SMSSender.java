package tuwien.inso.mnsa.smssender;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.Properties;

import tuwien.inso.mnsa.smssender.sms.GSMNumber;
import tuwien.inso.mnsa.smssender.sms.SMS;
import tuwien.inso.mnsa.smssender.sms.SMSException;
import tuwien.inso.mnsa.smssender.translator.GSM0338Encoder;
import tuwien.inso.mnsa.smssender.translator.MappingException;

public class SMSSender {

	private static final String PROPERTIES_FILENAME = "sendsms.properties";
	private static final String PROPERTIES_PORT_IDENTIFIER = "port";
	private static final String PROPERTIES_CSV_FILE_IDENTIFIER = "csvfile";
	private static final int COMMUNICATION_TIMEOUT = 100;

	public static void main(String[] args) throws PortInUseException, NoSuchPortException, UnsupportedCommOperationException, IOException, MappingException, SMSException {
		FileInputStream propertiesInStream;
		try {
			propertiesInStream = new FileInputStream(PROPERTIES_FILENAME);
		} catch (FileNotFoundException e) {
			System.err.println("File " + PROPERTIES_FILENAME + " not found.");
			return;
		}

		Properties properties = new Properties();
		try {
			properties.load(propertiesInStream);
		} catch (IOException e) {
			System.err.println("Can't read properties file.");
			return;
		}

		if (!properties.containsKey(PROPERTIES_PORT_IDENTIFIER) || !properties.containsKey(PROPERTIES_CSV_FILE_IDENTIFIER)) {
			System.err.println("No " + PROPERTIES_PORT_IDENTIFIER + " or " + PROPERTIES_CSV_FILE_IDENTIFIER + " specified in " + PROPERTIES_FILENAME);
			return;
		}

		@SuppressWarnings("unchecked")
		Enumeration<CommPortIdentifier> ports = CommPortIdentifier.getPortIdentifiers();

		System.out.println("Listing ports:");
		while (ports.hasMoreElements()) {
			CommPortIdentifier port = ports.nextElement();

			System.out.println(port.getName() + " (" + port.getPortType() + ", current owner " + port.getCurrentOwner() + ")");
		}
		System.out.println("Port listing done.");

		BufferedReader smsReader;
		try {
			smsReader = new BufferedReader(new InputStreamReader(new FileInputStream(properties.getProperty(PROPERTIES_CSV_FILE_IDENTIFIER)), Charset.forName("UTF-8")));
		} catch (FileNotFoundException e) {
			System.err.println("Datafile " + properties.getProperty(PROPERTIES_CSV_FILE_IDENTIFIER) + " not found.");
			return;
		}

		String line;
		while ((line = smsReader.readLine()) != null) {
			String number, text;
			int indexOfFirstComma = line.indexOf(',');
			number = line.substring(0, indexOfFirstComma);
			text = line.substring(indexOfFirstComma + 1);

			byte[] gsm0338text = GSM0338Encoder.encode(text);
			String decodedGsm0338text = GSM0338Encoder.decode(gsm0338text);

			System.out.println("[" + number + "] " + text);
			System.out.println("\t = " + Utils.bytesToHex(gsm0338text));
			System.out.println("\t = " + decodedGsm0338text);

			//SMS sms = SMS.generateSimpleSMS(GSMNumber.fromInternational(number), text);
			SMS[] sms = SMS.generateConcatenatedSMS(GSMNumber.fromInternational(number), text);

			for (SMS s : sms) {
				System.out.println("\tPDU: " + Utils.bytesToHex(s.getPDU()));
			}
		}

		smsReader.close();

		if (true)
			return;

		@SuppressWarnings("unchecked")
		SerialPort serialPort;
		serialPort = CommPortIdentifier.getPortIdentifier(properties.getProperty(PROPERTIES_PORT_IDENTIFIER)).open(SMSSender.class.getCanonicalName(), COMMUNICATION_TIMEOUT);
		serialPort.enableReceiveTimeout(COMMUNICATION_TIMEOUT);

		BufferedReader reader = new BufferedReader(new InputStreamReader(serialPort.getInputStream(), Charset.forName("US-ASCII")));
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(serialPort.getOutputStream(), Charset.forName("US-ASCII")), true);

		String atz = sendCommandReadAnswer(reader, writer, "ATZ", 1)[0];
		System.out.println("Answer to ATZ: " + atz);

		String cpin = sendCommandReadAnswer(reader, writer, "AT+CPIN?", 2)[0];
		System.out.println("Answer to AT+CPIN?: " + cpin);

		boolean pinlock = !cpin.contains("READY");

		if (pinlock) {
			System.out.print("Enter PIN: ");
			String pin = new String(System.console().readPassword());

			cpin = sendCommandReadAnswer(reader, writer, "AT+CPIN=" + pin, 1)[0];
			System.out.println("Answer to AT+CPIN=****: " + cpin);

			if (!"OK".equals(cpin)) {
				System.out.println("PIN unlock failed :( received from modem: \"" + cpin + "\"");
				return;
			}
		}

		do {
			String creg = sendCommandReadAnswer(reader, writer, "AT+CREG?", 2)[0];
			System.out.println("Answer to AT+CREG?: " + creg);
			String[] cregS = creg.split(",");
			if (cregS.length < 2) {
				System.out.println("Unparseable AT+CREG? answer: " + creg);
				return;
			}
			creg = cregS[1];
			if (creg.equals("1") || creg.equals("5")) {
				break;
			}
			System.out.print("Waiting for network... ");

			try {
				Thread.sleep(2000);
			} catch (InterruptedException ignored) {
			}
		} while (true);

		sendCommandReadAnswer(reader, writer, "AT+COPS=3,0", 1);

		String cops = sendCommandReadAnswer(reader, writer, "AT+COPS?", 1)[0];
		System.out.println("Answer to AT+COPS?: " + cops);

		//byte[] pdu = 
		//sendCommandReadAnswer(reader, writer, "AT+CMGS=" + testPdu.length, 1);

		reader.close();
		writer.close();

		serialPort.close();

	}

	private static String[] sendCommandReadAnswer(BufferedReader reader, PrintWriter writer, String command, int lines) throws IOException {
		writer.print(command);
		writer.print("\r\n");
		writer.flush();

		String[] ret = new String[lines];
		int i = 0;
		String line;

		while (i < lines) {
			if ((line = ret[i] = reader.readLine()) == null)
				break;

			line = line.trim();

			if ("".equals(line) || command.equals(line))
				continue;
			i++;
		}

		return ret;
	}
}
