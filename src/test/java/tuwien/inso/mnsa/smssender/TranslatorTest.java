package tuwien.inso.mnsa.smssender;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class TranslatorTest {

	private CodeTranslator translator;

	@Before
	public void startup() {
		translator = new Ascii7BitTranslator();
	}

	@Test
	public void encodeTest1() {
		byte[] input = hexToBytes("7F7F7F7F7F7F7F7F");
		byte[] output = translator.encode(input);
		assertEquals("length", 7, output.length);
		assertEquals("byte 0", (byte) 0xff, output[0]);
		assertEquals("byte 1", (byte) 0xff, output[1]);
		assertEquals("byte 2", (byte) 0xff, output[2]);
		assertEquals("byte 3", (byte) 0xff, output[3]);
		assertEquals("byte 4", (byte) 0xff, output[4]);
		assertEquals("byte 5", (byte) 0xff, output[5]);
		assertEquals("byte 6", (byte) 0xff, output[6]);
	}

	@Test
	public void encodeTest2() {
		byte[] input = hexToBytes("7F7F7F7F7F7F7F7F7F");
		byte[] output = translator.encode(input);
		assertEquals("length", 8, output.length);
		assertEquals("byte 0", (byte) 0xff, output[0]);
		assertEquals("byte 1", (byte) 0xff, output[1]);
		assertEquals("byte 2", (byte) 0xff, output[2]);
		assertEquals("byte 3", (byte) 0xff, output[3]);
		assertEquals("byte 4", (byte) 0xff, output[4]);
		assertEquals("byte 5", (byte) 0xff, output[5]);
		assertEquals("byte 6", (byte) 0xff, output[6]);
		assertEquals("byte 7", (byte) 0xfe, output[7]);
	}

	@Test
	public void encodeTest3() {
		byte[] input = hexToBytes("5555555555");
		byte[] output = translator.encode(input);
		assertEquals("length", 5, output.length);
		assertEquals("byte 0", (byte) 0xab, output[0]);
		assertEquals("byte 1", (byte) 0x56, output[1]);
		assertEquals("byte 2", (byte) 0xad, output[2]);
		assertEquals("byte 3", (byte) 0x5a, output[3]);
		assertEquals("byte 4", (byte) 0xa0, output[4]);
	}

	@Test
	public void decodeTest1() {
		byte[] input = hexToBytes("FFFFFFFFFFFFFF");
		byte[] output = translator.decode(input);
		assertEquals("length", 8, output.length);
		assertEquals("byte 0", 0x7f, output[0]);
		assertEquals("byte 1", 0x7f, output[1]);
		assertEquals("byte 2", 0x7f, output[2]);
		assertEquals("byte 3", 0x7f, output[3]);
		assertEquals("byte 4", 0x7f, output[4]);
		assertEquals("byte 5", 0x7f, output[5]);
		assertEquals("byte 6", 0x7f, output[6]);
		assertEquals("byte 7", 0x7f, output[7]);
	}

	@Test
	public void decodeTest2() {
		byte[] input = hexToBytes("FFFFFFFFFFFFFFFE");
		byte[] output = translator.decode(input);
		assertEquals("length", 10, output.length);
		assertEquals("byte 0", 0x7f, output[0]);
		assertEquals("byte 1", 0x7f, output[1]);
		assertEquals("byte 2", 0x7f, output[2]);
		assertEquals("byte 3", 0x7f, output[3]);
		assertEquals("byte 4", 0x7f, output[4]);
		assertEquals("byte 5", 0x7f, output[5]);
		assertEquals("byte 6", 0x7f, output[6]);
		assertEquals("byte 7", 0x7f, output[7]);
		assertEquals("byte 8", 0x7f, output[8]);
	}

	@Test
	public void decodeTest3() {
		byte[] input = hexToBytes("AB56AD5AA0");
		byte[] output = translator.decode(input);
		assertEquals("length", 6, output.length);
		assertEquals("byte 0", 0x55, output[0]);
		assertEquals("byte 1", 0x55, output[1]);
		assertEquals("byte 2", 0x55, output[2]);
		assertEquals("byte 3", 0x55, output[3]);
		assertEquals("byte 4", 0x55, output[4]);
	}


	@Test
	public void selfTest() {
		String hexInput = "FedCA71337";

		byte[] inBytes = hexToBytes(hexInput);
		assertEquals("byte 0", (byte) 0xfe, inBytes[0]);
		assertEquals("byte 1", (byte) 0xdc, inBytes[1]);
		assertEquals("byte 2", (byte) 0xa7, inBytes[2]);
		assertEquals("byte 3", (byte) 0x13, inBytes[3]);
		assertEquals("byte 4", (byte) 0x37, inBytes[4]);

		String hexOutput = bytesToHex(inBytes);
		assertEquals("all", hexInput.toLowerCase(), hexOutput.toLowerCase());
	}

	// From http://stackoverflow.com/questions/9655181/convert-from-byte-array-to-hex-string-in-java
	private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

	private static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		int v;
		for (int j = 0; j < bytes.length; j++) {
			v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	public static byte[] hexToBytes(String string) {
		byte[] data = new byte[string.length() / 2];

		int v;
		for (int i = 0; i < data.length; i++) {
			v = Character.digit(string.charAt(i * 2), 16) << 4;
			v |= Character.digit(string.charAt(i * 2 + 1), 16);
			data[i] = (byte) v;
		}
		return data;
	}
}
