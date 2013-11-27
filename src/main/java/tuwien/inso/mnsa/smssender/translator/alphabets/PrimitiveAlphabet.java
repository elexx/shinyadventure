package tuwien.inso.mnsa.smssender.translator.alphabets;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import tuwien.inso.mnsa.smssender.translator.MappingException;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class PrimitiveAlphabet extends Alphabet {
	private final BiMap<Character, Byte> forward;
	private final BiMap<Byte, Character> reverse;

	public PrimitiveAlphabet(BiMap<Character, Byte> mapping) {
		forward = mapping;
		reverse = mapping.inverse();
	}

	@Override
	public boolean isRepresentable(char input) {
		return forward.containsKey(input);
	}

	@Override
	public void writeTo(char input, OutputStream stream) throws IOException, MappingException {
		if (!isRepresentable(input))
			throw new MappingException("invalid character (value " + ((int) input) + "): '" + input + "'");

		stream.write(forward.get(input));
	}

	@Override
	public char readFrom(InputStream stream) throws IOException, EOFException, MappingException {
		int r = stream.read();
		if (r == -1)
			throw new EOFException();
		else if (!reverse.containsKey((byte)r))
			throw new MappingException("invalid byte: " + r);
		else
			return reverse.get((byte)r);
	}

	public boolean isRepresentable(byte input) {
		return reverse.containsKey(input);
	}

	public char getCharacter(byte r) {
		return reverse.get(r);
	}

	public static PrimitiveAlphabet fromValueList(List<Character> characters) {
		BiMap<Character, Byte> mapping = HashBiMap.create();

		for (int i = 0; i < characters.size(); i++) {
			if (characters.get(i) != null)
				mapping.put(characters.get(i), (byte) i);
		}

		return new PrimitiveAlphabet(mapping);
	}

	public static PrimitiveAlphabet fromForwardMap(Map<Character, Byte> forward) {
		BiMap<Character, Byte> mapping = HashBiMap.create();

		for (char c : forward.keySet()) {
			mapping.put(c, forward.get(c));
		}

		return new PrimitiveAlphabet(mapping);
	}

	public static PrimitiveAlphabet fromReverseMap(Map<Byte, Character> reverse) {
		BiMap<Character, Byte> mapping = HashBiMap.create();

		for (byte b : reverse.keySet()) {
			mapping.put(reverse.get(b), b);
		}

		return new PrimitiveAlphabet(mapping);
	}
}
