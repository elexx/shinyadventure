package tuwien.inso.mnsa.smssender.sms;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import tuwien.inso.mnsa.smssender.Utils;
import tuwien.inso.mnsa.smssender.translator.GSM0338Encoder;
import tuwien.inso.mnsa.smssender.translator.MappingException;

public class SMS {
	public static final int MAXIMUM_PDU_LENGTH = 140;

	public static final int UDHI_BIT = 7;
	public static final int STATUS_REPORT_BIT = 6;

	public static final byte PROTOCOL_IDENTIFIER = 0;
	public static final byte DATA_CODING_SCHEME = 0;

	private final byte[] udh, payload;
	private final byte[] pdu;

	public SMS(GSMNumber smsCenter, SMSType type, boolean requestStatusReport, byte referenceNumber, GSMNumber destination, byte[] udh, byte[] payload, int septetCount) throws MappingException, SMSException {
		if (payload.length > MAXIMUM_PDU_LENGTH) {
			throw new SMSException("text too long");
		}

		if (udh == null)
			udh = new byte[0];

		this.udh = udh;
		this.payload = payload;

		byte[] smsCenterRaw;
		if (smsCenter != null)
			smsCenterRaw = smsCenter.getEncoded();
		else
			smsCenterRaw = new byte[] { (byte) 0 };

		byte[] smsReceiverRaw = destination.getEncoded(false);
		byte messageFlags = 0;
		if (udh != null)
			messageFlags |= 1 << UDHI_BIT;
		if (requestStatusReport)
			messageFlags |= 1 << STATUS_REPORT_BIT;
		messageFlags |= type.getByte();

		int paddingBits = (7 - (udh.length * 8)) % 7;
		if (paddingBits < 0)
			paddingBits += 7;
		byte[] shiftedPayload = shiftRight(payload, paddingBits);
		System.out.println("pre:  " + Utils.bytesToHex(payload));
		System.out.println("post: " + Utils.bytesToHex(shiftedPayload));

		pdu = new byte[smsCenterRaw.length + 2 + smsReceiverRaw.length + 3 + udh.length + shiftedPayload.length];
		int i = 0;

		System.arraycopy(smsCenterRaw, 0, pdu, i, smsCenterRaw.length);
		i += smsCenterRaw.length;
		pdu[i++] = messageFlags;
		pdu[i++] = referenceNumber;
		System.arraycopy(smsReceiverRaw, 0, pdu, i, smsReceiverRaw.length);
		i += smsReceiverRaw.length;
		pdu[i++] = PROTOCOL_IDENTIFIER;
		pdu[i++] = DATA_CODING_SCHEME;
		pdu[i++] = (byte) ((udh.length * 8 + paddingBits) / 7 + septetCount);
		System.arraycopy(udh, 0, pdu, i, udh.length);
		i += udh.length;
		System.arraycopy(shiftedPayload, 0, pdu, i, shiftedPayload.length);
	}

	static byte[] shiftRight(byte[] bytes, int bits) {
		if (bits == 0)
			return bytes;

		int lsb = 0;
		ByteBuffer buf = ByteBuffer.allocate(bytes.length + 1);
		int lsbMask = (1 << bits) - 1;
		System.out.println(">> " + bits + ": " + lsbMask);

		for (int i = 0; i < bytes.length; i++) {
			// store the least significant bits
			int newlsb = bytes[i] & lsbMask;

			int current = (bytes[i] & 0xFF) >>> bits;
			current |= (lsb << (8 - bits));
			buf.put((byte) current);

			lsb = newlsb;
		}

		buf.put((byte) (lsb << bits));

		return Arrays.copyOfRange(buf.array(), 0, buf.position());
	}

	public byte[] getPDU() {
		return pdu;
	}

	public byte[] getPayload() {
		return payload;
	}

	public byte[] getUdh() {
		return udh;
	}

	public enum SMSType {
		SMS_DELIVER(true, false, false), SMS_DELIVER_REPORT(false, false, false), SMS_STATUS_REPORT(true, true, false), SMS_COMMAND(false, true, false), SMS_SUBMIT_REPORT(true, false, true), SMS_SUBMIT(false, false, true), RESERVED1(false, true, true), RESERVED2(true, true, true);

		private final boolean receiving, msb, lsb;

		private SMSType(boolean receiving, boolean msb, boolean lsb) {
			this.receiving = receiving;
			this.lsb = lsb;
			this.msb = msb;
		}

		public byte getByte() {
			return (byte) ((lsb ? 1 : 0) | (msb ? 2 : 0));
		}

		public boolean isReceiving() {
			return receiving;
		}

		public boolean getLSB() {
			return lsb;
		}

		public boolean getMSB() {
			return msb;
		}
	}

	public static SMS generateSimpleSMS(GSMNumber destination, String text) throws MappingException, SMSException {
		return new SMS(null, SMSType.SMS_SUBMIT, false, (byte) 0, destination, null, GSM0338Encoder.encode(text), (byte) GSM0338Encoder.getRawLength(text));
	}

	private static final Random random = new Random();

	public static SMS[] generateConcatenatedSMS(GSMNumber destination, String text) throws MappingException, SMSException {
		int split = 169;
		List<String> splits = new ArrayList<>(text.length() / split + 1);
		while (true) {
			if (text.length() <= split) {
				splits.add(text);
				break;
			}
			splits.add(text.substring(0, split));
			text = text.substring(split);
		}

		byte sequence = (byte) random.nextInt(0xFF);
		SMS[] ret = new SMS[splits.size()];
		int current = 0;

		for (String sub : splits) {
			byte[] udh = new byte[] { 5, 0, 3, sequence, (byte) splits.size(), (byte) (current + 1) };
			ret[current++] = new SMS(null, SMSType.SMS_SUBMIT, true, (byte) 0, destination, udh, GSM0338Encoder.encode(sub), (byte) GSM0338Encoder.getRawLength(sub));
		}

		return ret;

	}
}
