package tuwien.inso.mnsa.smssender.translator.alphabets;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.CharacterCodingException;

import tuwien.inso.mnsa.smssender.translator.MappingException;

public class EscapeShiftingAlphabet extends Alphabet {
	protected final PrimitiveAlphabet baseAlphabet;
	protected final byte shiftByte;
	protected final Alphabet shiftTarget;

	public EscapeShiftingAlphabet(PrimitiveAlphabet baseAlphabet, byte shiftByte, Alphabet shiftTarget) {
		this.baseAlphabet = baseAlphabet;
		this.shiftByte = shiftByte;
		this.shiftTarget = shiftTarget;
	}

	@Override
	public boolean isRepresentable(char input) {
		return baseAlphabet.isRepresentable(input) || shiftTarget.isRepresentable(input);
	}

	@Override
	public void writeTo(char input, OutputStream stream) throws MappingException, IOException {
		if (baseAlphabet.isRepresentable(input))
			baseAlphabet.writeTo(input, stream);
		else {
			stream.write(shiftByte);
			shiftTarget.writeTo(input, stream);
		}
	}

	@Override
	public char readFrom(InputStream stream) throws EOFException, MappingException, IOException {
		int r = stream.read();

		if (r == -1)
			throw new EOFException();
		else if (r == shiftByte)
			return shiftTarget.readFrom(stream);
		else if (baseAlphabet.isRepresentable((byte) r))
			return baseAlphabet.getCharacter((byte) r);
		else
			throw new CharacterCodingException();
	}
}
