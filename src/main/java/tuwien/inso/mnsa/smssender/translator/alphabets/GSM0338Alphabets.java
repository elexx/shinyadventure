package tuwien.inso.mnsa.smssender.translator.alphabets;

import java.util.Arrays;
import java.util.Map;

import com.google.common.collect.Maps;

public class GSM0338Alphabets {
	private GSM0338Alphabets() {
	}

	public static final EscapeShiftingAlphabet DEFAULT_GSM0338;
	private static final PrimitiveAlphabet BASIC_CHARACTER_SET;
	private static final PrimitiveAlphabet BASIC_CHARACTER_SET_EXTENSION;

	static {
		Map<Character, Byte> basicExtension = Maps.newHashMapWithExpectedSize(9);

		basicExtension.put('^', (byte) 0x14);
		basicExtension.put('{', (byte) 0x28);
		basicExtension.put('}', (byte) 0x29);
		basicExtension.put('\\', (byte) 0x2F);
		basicExtension.put('[', (byte) 0x3C);
		basicExtension.put('~', (byte) 0x3D);
		basicExtension.put(']', (byte) 0x3E);
		basicExtension.put('|', (byte) 0x40);
		basicExtension.put('€', (byte) 0x65);

		BASIC_CHARACTER_SET = PrimitiveAlphabet.fromValueList(Arrays.asList('@', '£', '$', '¥', 'è', 'é', 'ù', 'ì', 'ò', 'Ç', '\n', 'Ø', 'ø', '\r', 'Å', 'å', 'Δ', '_', 'Φ', 'Γ', 'Λ', 'Ω', 'Π', 'Ψ', 'Σ', 'Θ', 'Ξ', null, 'Æ', 'æ', 'ß', 'É', ' ', '!', '"', '#', '¤', '%', '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ':', ';', '<', '=', '>', '?', '¡', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'Ä', 'Ö', 'Ñ', 'Ü', '§', '¿', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'ä', 'ö', 'ñ', 'ü', 'à'));
		BASIC_CHARACTER_SET_EXTENSION = PrimitiveAlphabet.fromForwardMap(basicExtension);

		DEFAULT_GSM0338 = new EscapeShiftingAlphabet(BASIC_CHARACTER_SET, (byte) 0x1B, BASIC_CHARACTER_SET_EXTENSION);
	}

}
