package tuwien.inso.mnsa.smssender.translator;

import tuwien.inso.mnsa.smssender.translator.alphabets.GSM0338Alphabets;

public class GSM0338Encoder {

	private GSM0338Encoder() {
	}
	
	public static byte[] encodeToOctets(String input) throws MappingException {
		return GSM0338Alphabets.DEFAULT_GSM0338.getBytes(input);
	}
	
	public static String decodeFromOctets(byte[] octets) throws MappingException {
		return GSM0338Alphabets.DEFAULT_GSM0338.getString(octets);
	}

	public static byte[] encodeToPackedSeptets(String input) throws MappingException {
		return Interleaved7BitTranslator.packSeptets(encodeToOctets(input));
	}

	public static String decodeFromPackedSeptets(byte[] input) throws MappingException {
		byte[] raw = Interleaved7BitTranslator.unpackSeptets(input);
		return decodeFromOctets(raw);
	}
}
