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
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import tuwien.inso.mnsa.smssender.sms.GSMNumber;
import tuwien.inso.mnsa.smssender.sms.SMS;
import tuwien.inso.mnsa.smssender.sms.SMS.SMSType;
import tuwien.inso.mnsa.smssender.sms.SMSException;
import tuwien.inso.mnsa.smssender.sms.SMSGenerator;
import tuwien.inso.mnsa.smssender.translator.MappingException;

public class SMSSender {

	private static final String PROPERTIES_FILENAME = "sendsms.properties";
	private static final String PROPERTIES_PORT_IDENTIFIER = "port";
	private static final String PROPERTIES_CSV_FILE_IDENTIFIER = "csvfile";
	private static final int COMMUNICATION_TIMEOUT = 500;
	private static final int SMS_SENDING_TIMEOUT = 10000;

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

		List<Template> templates = new LinkedList<>();

		try (BufferedReader smsReader = new BufferedReader(new InputStreamReader(new FileInputStream(properties.getProperty(PROPERTIES_CSV_FILE_IDENTIFIER)), Charset.forName("UTF-8")))) {
			String line;
			while ((line = smsReader.readLine()) != null) {
				String number, text;
				int indexOfFirstComma = line.indexOf(',');
				number = line.substring(0, indexOfFirstComma);
				text = line.substring(indexOfFirstComma + 1);

				templates.add(new Template(number, text));
			}
		} catch (FileNotFoundException e) {
			System.err.println("Datafile " + properties.getProperty(PROPERTIES_CSV_FILE_IDENTIFIER) + " not found.");
			return;
		}

		SerialPort serialPort;
		serialPort = CommPortIdentifier.getPortIdentifier(properties.getProperty(PROPERTIES_PORT_IDENTIFIER)).open(SMSSender.class.getCanonicalName(), COMMUNICATION_TIMEOUT);
		serialPort.enableReceiveTimeout(COMMUNICATION_TIMEOUT);

		BufferedReader reader = new BufferedReader(new InputStreamReader(serialPort.getInputStream(), Charset.forName("US-ASCII")));
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(serialPort.getOutputStream(), Charset.forName("US-ASCII")), true);

		try {
			writer.write((char) 0x1A + "\r\nATZ\r\nATZ\r\n");
			writer.flush();

			// quick method of draining the input stream since unfortunately, the serial comm port stream does not obey to basic i/o rules :-(
			while (true) {
				try {
					reader.read();
				} catch (IOException ex) {
					// let's just assume this is timeout, if not, exceptions are going be thrown afterwards anyway
					break;
				}
			}

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

			String cops = sendCommandReadAnswer(reader, writer, "AT+COPS?", 2)[0];
			System.out.println("Answer to AT+COPS?: " + cops);

			serialPort.enableReceiveTimeout(SMS_SENDING_TIMEOUT);
			try {
				for (Template template : templates) {
					System.out.print("Sending " + template.getText().length() + " characters to " + template.getDestinationNumber() + "...");
					SMS[] messages = SMSGenerator.generateConcatenatedSMS(null, SMSType.SMS_SUBMIT, GSMNumber.fromInternational(template.getDestinationNumber()), (byte) 0, template.getText());

					if (messages.length == 1)
						System.out.print(" (no split) ");
					else
						System.out.print(" (split into " + messages.length + " parts) ");

					for (SMS sms : messages) {
						byte[] pdu = sms.getPDU();
						int messageBytesExcludingSMSC = pdu.length - sms.getSMSCDescriptorLength();

						String command = "AT+CMGS=" + messageBytesExcludingSMSC;
						String pduS = Utils.bytesToHex(pdu);

						sendCommandReadAnswer(reader, writer, command, 0);
						sendCommandReadAnswer(reader, writer, pduS + (char) 0x1A, 0);

						boolean ok = false;
						while (true) {
							String response = readAnswer(reader);
							if (response == null)
								break;
							if (response.equals(command))
								continue;
							if (response.startsWith(">"))
								continue;
							if (pduS.contains(response))
								continue;
							if (response.startsWith("+CMGS: ")) {
								System.out.print("(Assigend ID: " + response + ") ");
								continue;
							}
							if (response.equals("OK")) {
								ok = true;
								System.out.print(" (OK) ");
								break;
							}
							System.out.println("Unknown SMS sending response: " + response);
							System.out.println("  (PDU: " + pduS + ")");
							return;
						}

						if (ok) {
							System.out.print("sent ");
						} else {
							System.out.println("No OK answer received!");
							return;
						}

					}

					if (messages.length > 1)
						System.out.println(" (all parts sent)");
					else 
						System.out.println();
				}
			} finally {
				serialPort.enableReceiveTimeout(COMMUNICATION_TIMEOUT);
			}

		} finally {
			reader.close();
			writer.close();

			serialPort.close();
		}

	}

	private static String[] sendCommandReadAnswer(BufferedReader reader, PrintWriter writer, String command, int lines) throws IOException {
		writer.print(command);
		writer.print("\r\n");
		writer.flush();

		String[] ret = new String[lines];
		int i = 0;
		String line;

		while (i < lines) {
			line = readAnswer(reader);
			if (line == null)
				break;
			else if (line.equals(command))
				continue;
			else
				ret[i++] = line;
		}

		return ret;
	}

	private static String readAnswer(BufferedReader reader) throws IOException {
		String line;
		do {
			if ((line = reader.readLine()) == null)
				break;
		} while ("".equals(line.trim()));
		return line;
	}
}
