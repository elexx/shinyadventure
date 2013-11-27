package tuwien.inso.mnsa.smssender.translator;

import tuwien.inso.mnsa.smssender.translator.alphabets.GSM0338Alphabets;

public class GSM0338Encoder {

	private GSM0338Encoder() {
	}

	public static byte[] encode(String input) throws MappingException {
		byte[] raw = GSM0338Alphabets.DEFAULT_GSM0338.getBytes(input);
		return Interleaved7BitTranslator.encode(raw);
	}

	public static String decode(byte[] input) throws MappingException {
		byte[] raw = Interleaved7BitTranslator.decode(input);
		return GSM0338Alphabets.DEFAULT_GSM0338.getString(raw);
	}
}
