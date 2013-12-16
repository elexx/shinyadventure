package tuwien.inso.mnsa.smssender.sms;

import java.util.ArrayList;
import java.util.List;

public class GSMNumber {

	public static final byte NATIONAL = (byte) 0x81;
	public static final byte INTERNATIONAL = (byte) 0x91;

	private final byte[] digits;
	private final byte type;

	private GSMNumber(byte type, byte[] digits) throws SMSException {
		this.digits = digits;
		this.type = type;
	}

	public static GSMNumber fromInternational(String number) throws SMSException {
		return new GSMNumber(INTERNATIONAL, getDigits(number));
	}

	public static GSMNumber fromNational(String number) throws SMSException {
		return new GSMNumber(NATIONAL, getDigits(number));
	}

	public static GSMNumber fromCustomType(String number, byte type) throws SMSException {
		return new GSMNumber(type, getDigits(number));
	}

	private static byte[] getDigits(String number) throws SMSException {
		List<Byte> bytes = new ArrayList<>();
		for (int i = 0; i < number.length(); i++) {
			char c = number.charAt(i);
			if (c == '+' || c == 'P' || c == 'p')
				continue;
			if (c < '0' || c > '9')
				throw new SMSException("invalid digit in number: " + c);
			bytes.add((byte) (c - '0'));
		}

		byte[] ret = new byte[bytes.size()];
		for (int i = 0; i < ret.length; i++)
			ret[i] = bytes.get(i);

		return ret;
	}

	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder();
		if (type == INTERNATIONAL)
			ret.append('+');
		for (byte b : digits)
			ret.append('0' + b);
		return ret.toString();
	}

	public byte getType() {
		return type;
	}

	public byte[] getDigits() {
		return digits;
	}

	public byte[] getEncoded() {
		return getEncoded(true);
	}

	public byte[] getEncoded(boolean includeTypeInLength) {
		byte[] ret = new byte[2 + (digits.length + 1) / 2];
		ret[0] = (byte) (digits.length + (includeTypeInLength ? 1 : 0));
		ret[1] = type;
		for (int i = 0; i + 1 < digits.length; i += 2)
			ret[2 + (i / 2)] = (byte) ((digits[i + 1] << 4) + digits[i]);
		if (digits.length % 2 == 1)
			ret[ret.length - 1] = (byte) (digits[digits.length - 1] | 0xF0);

		return ret;
	}
}
