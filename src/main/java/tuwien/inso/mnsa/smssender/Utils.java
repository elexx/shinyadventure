package tuwien.inso.mnsa.smssender;

public class Utils {

	// From http://stackoverflow.com/questions/9655181/convert-from-byte-array-to-hex-string-in-java
	private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

	public static String bytesToDec(byte[] bytes) {
		StringBuffer buffer = new StringBuffer();
		for (byte b : bytes) {
			buffer.append(b & 0xFF);
		}
		return buffer.toString();
	}

	public static String bytesToHex(byte bytee) {
		return bytesToHex(new byte[] { bytee });
	}

	public static String bytesToHex(byte[] bytes) {
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

	public static String byteToHex(byte b) {
		int v = b & 0xFF;
		return new String(new char[] { hexArray[v >>> 4], hexArray[v & 0x0F] });
	}
}
