package tuwien.inso.mnsa.smssender.translator.alphabets;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import tuwien.inso.mnsa.smssender.translator.MappingException;

/**
 * Represents a 1:1 mapping between characters and byte sequences.
 * 
 */
public abstract class Alphabet {

	/**
	 * Checks whether a given character is representable by the current
	 * alphabet.
	 * 
	 * @param input
	 * @return
	 */
	public abstract boolean isRepresentable(char input);

	/**
	 * Writes a single character to an output stream. If the character is not
	 * representable (if {@link #isRepresentable(char)} returns false), a
	 * CharacterCodingException is thrown.
	 * 
	 * @param input
	 * @param stream
	 * @throws MappingException
	 *             if the character is not representable
	 * @throws IOException
	 */
	public abstract void writeTo(char input, OutputStream stream) throws MappingException, IOException;

	/**
	 * Reads a single character from an input stream. If the input stream
	 * contains invalid data, a CharacterCodingException is thrown.
	 * 
	 * @param stream
	 * @return
	 * @throws EOFException
	 *             if an EOF occurs before one character could be fully read
	 * @throws MappingException
	 *             if the stream contains non-parseable data
	 * @throws IOException
	 */
	public abstract char readFrom(InputStream stream) throws EOFException, MappingException, IOException;

	/**
	 * Writes a given string to the output stream. This is a convenience method
	 * for {@link #writeTo(char, OutputStream)}.
	 * 
	 * @param input
	 * @param stream
	 * @throws MappingException
	 *             if one of the characters it not represantable. In this case,
	 *             the characters prior to the non-representable character will
	 *             have been written to the stream.
	 * @throws IOException
	 */
	public void writeTo(String input, OutputStream stream) throws IOException, MappingException {
		for (char c : input.toCharArray())
			writeTo(c, stream);
	}

	/**
	 * Reads a stream until an EOF occurs or non-parseable data is encountered
	 * in the stream. This is a convenience method for
	 * {@link #readFrom(InputStream)}
	 * 
	 * @param stream
	 * @return
	 * @throws MappingException
	 *             if the stream contained non-parseable data at some point.
	 * @throws IOException
	 */
	public String readFully(InputStream stream) throws IOException, MappingException {
		StringBuilder result = new StringBuilder();

		do {
			try {
				result.append(readFrom(stream));
			} catch (EOFException e) {
				break;
			}
		} while (true);

		return result.toString();
	}

	/**
	 * Returns the byte array representation of a given input. This is a
	 * convenience method for {@link #writeTo(String, OutputStream)}.
	 * 
	 * @param input
	 * @return
	 */
	public byte[] getBytes(String input) throws MappingException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			writeTo(input, bos);
		} catch (IOException e) {
			// since ByteArrayOutputStream does not throw I/O exceptions, this can normally never happen and probably represents some major problem
			throw new IOError(e);
		}
		return bos.toByteArray();
	}

	/**
	 * Returns the String representation of a given input. This is a convenience
	 * method for {@link #readFrom(InputStream)} and/or
	 * {@link #readFully(InputStream)}.
	 * 
	 * @param input
	 * @return
	 */
	public String getString(byte[] bytes) throws MappingException {
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		try {
			return readFully(bis);
		} catch (IOException e) {
			// since ByteArrayOutputStream does not throw I/O exceptions, this can normally never happen and probably represents some major problem
			throw new IOError(e);
		}
	}
}
