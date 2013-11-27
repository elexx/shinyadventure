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

import tuwien.inso.mnsa.smssender.translator.GSM0338Encoder;
import tuwien.inso.mnsa.smssender.translator.MappingException;

public class SMSSender {

	private static final String PROPERTIES_FILENAME = "sendsms.properties";
	private static final String PROPERTIES_PORT_IDENTIFIER = "port";
	private static final String PROPERTIES_CSV_FILE_IDENTIFIER = "csvfile";
	private static final int COMMUNICATION_TIMEOUT = 100;

	public static void main(String[] args) throws PortInUseException, NoSuchPortException, UnsupportedCommOperationException, IOException, MappingException {
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
		}

		smsReader.close();

		@SuppressWarnings("unchecked")
		SerialPort serialPort;
		serialPort = CommPortIdentifier.getPortIdentifier(properties.getProperty(PROPERTIES_PORT_IDENTIFIER)).open(SMSSender.class.getCanonicalName(), COMMUNICATION_TIMEOUT);
		serialPort.enableReceiveTimeout(COMMUNICATION_TIMEOUT);

		BufferedReader reader = new BufferedReader(new InputStreamReader(serialPort.getInputStream(), Charset.forName("US-ASCII")));
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(serialPort.getOutputStream(), Charset.forName("US-ASCII")), true);

		reader.close();
		writer.close();

		serialPort.close();

	}
}
