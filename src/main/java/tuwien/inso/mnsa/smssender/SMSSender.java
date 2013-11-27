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
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

public class SMSSender {

	private static final String PROPERTIES_FILENAME = "sendsms.properties";
	private static final String PROPERTIES_PORT_IDENTIFIER = "port";
	private static final String PROPERTIES_CSV_FILE_IDENTIFIER = "csvfile";
	private static final int COMMUNICATION_TIMEOUT = 100;

	public static void main(String[] args) throws PortInUseException, NoSuchPortException, UnsupportedCommOperationException, IOException {
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

			byte[] gsm0338text = encode(text);
			String decodedGsm0338text = decode(gsm0338text);

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

	private static final List<Character> GSM7BIT_DEFAULT_CHARACTER_SET = Arrays.asList('@', '£', '$', '¥', 'è', 'é', 'ù', 'ì', 'ò', 'Ç', '\n', 'Ø', 'ø', '\r', 'Å', 'å', 'Δ', '_', 'Φ', 'Γ', 'Λ', 'Ω', 'Π', 'Ψ', 'Σ', 'Θ', 'Ξ', null, 'Æ', 'æ', 'ß', 'É', ' ', '!', '"', '#', '¤', '%', '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ':', ';', '<', '=', '>', '?', '¡', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'Ä', 'Ö', 'Ñ', 'Ü', '§', '¿', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'ä', 'ö', 'ñ', 'ü', 'à');

	private static byte[] encode(String text) {
		char[] cText = text.toCharArray();
		byte[] encoded = new byte[cText.length * 2];
		int i = 0;
		for(char c : cText) {
			int index = GSM7BIT_DEFAULT_CHARACTER_SET.indexOf(c);
			if (index == -1) {
				switch (c) {
				case '^':
					encoded[i++] = 0x1B;
					encoded[i++] = 0x14;
					break;
				case '{':
					encoded[i++] = 0x1B;
					encoded[i++] = 0x28;
					break;
				case '}':
					encoded[i++] = 0x1B;
					encoded[i++] = 0x29;
					break;
				case '\\':
					encoded[i++] = 0x1B;
					encoded[i++] = 0x2F;
					break;
				case '[':
					encoded[i++] = 0x1B;
					encoded[i++] = 0x3C;
					break;
				case '~':
					encoded[i++] = 0x1B;
					encoded[i++] = 0x3D;
					break;
				case ']':
					encoded[i++] = 0x1B;
					encoded[i++] = 0x3E;
					break;
				case '|':
					encoded[i++] = 0x1B;
					encoded[i++] = 0x40;
					break;
				case '€':
					encoded[i++] = 0x1B;
					encoded[i++] = 0x65;
					break;
				default:
					System.out.println("char [" + c + "] not part of gsm7 basic character set or the basic character set");
				}

			} else {
				encoded[i++] = (byte) (index & 0x7F);
			}
		}
		return Arrays.copyOf(encoded, i);
	}

	private static String decode(byte[] input) {
		StringBuilder text = new StringBuilder(input.length);
		for (int i = 0; i < input.length; i++) {
			if (input[i] > 0x7F) {
				System.out.println("char at position " + i + " is > 0x7F ... skipping");
			} else if (input[i] == 0x1B) {
				i++;
				switch (input[i]) {
				case 0x14:
					text.append('^');
					break;
				case 0x28:
					text.append('{');
					break;
				case 0x29:
					text.append('}');
					break;
				case 0x2F:
					text.append('\\');
					break;
				case 0x3C:
					text.append('[');
					break;
				case 0x3D:
					text.append('~');
					break;
				case 0x3E:
					text.append(']');
					break;
				case 0x40:
					text.append('|');
					break;
				case 0x65:
					text.append('€');
					break;
				default:
					System.out.println("unknown char at position " + i + " ... skipping");
				}
			} else {
				text.append(GSM7BIT_DEFAULT_CHARACTER_SET.get(input[i]));
			}
		}
		return text.toString();
	}

}
