package tuwien.inso.mnsa.smssender.sms;

import tuwien.inso.mnsa.smssender.translator.Interleaved7BitTranslator;
import tuwien.inso.mnsa.smssender.translator.MappingException;

public class SMS {
	public static final int MAXIMUM_PDU_LENGTH = 140;

	public static final int UDHI_BIT = 6;
	public static final int STATUS_REPORT_BIT = 5;

	public static final byte PROTOCOL_IDENTIFIER = 0;
	public static final byte DATA_CODING_SCHEME = 0;

	private final int smsDescriptorLength;

	private final byte[] udh, payload;
	private final byte[] pdu;

	SMS(GSMNumber smsCenter, SMSType type, boolean requestStatusReport, byte referenceNumber, GSMNumber destination, byte[] udh, byte[] payloadOctets) throws MappingException, SMSException {
		if (udh == null)
			udh = new byte[0];

		int udhVirtualSeptets = udh.length * 8 / 7 + ((udh.length * 8) % 7 == 0 ? 0 : 1);
		int effectiveSeptets = udhVirtualSeptets + payloadOctets.length;
		byte[] effectivePayloadUnpacked = new byte[effectiveSeptets];
		System.arraycopy(payloadOctets, 0, effectivePayloadUnpacked, udhVirtualSeptets, payloadOctets.length);
		byte[] effectivePayoadPacked = Interleaved7BitTranslator.packSeptets(effectivePayloadUnpacked);
		System.arraycopy(udh, 0, effectivePayoadPacked, 0, udh.length);

		this.udh = udh;
		this.payload = payloadOctets;

		byte[] smsCenterRaw;
		if (smsCenter != null)
			smsCenterRaw = smsCenter.getEncoded();
		else
			smsCenterRaw = new byte[] { (byte) 0 };

		smsDescriptorLength = smsCenterRaw.length;

		byte[] smsReceiverRaw = destination.getEncoded(false);
		byte messageFlags = 0;
		if (udh.length != 0)
			messageFlags |= 1 << UDHI_BIT;
		if (requestStatusReport)
			messageFlags |= 1 << STATUS_REPORT_BIT;
		messageFlags |= type.getByte();

		pdu = new byte[smsCenterRaw.length + 2 + smsReceiverRaw.length + 3 + effectivePayoadPacked.length];
		int i = 0;

		System.arraycopy(smsCenterRaw, 0, pdu, i, smsCenterRaw.length);
		i += smsCenterRaw.length;
		pdu[i++] = messageFlags;
		pdu[i++] = referenceNumber;
		System.arraycopy(smsReceiverRaw, 0, pdu, i, smsReceiverRaw.length);
		i += smsReceiverRaw.length;
		pdu[i++] = PROTOCOL_IDENTIFIER;
		pdu[i++] = DATA_CODING_SCHEME;
		pdu[i++] = (byte) effectiveSeptets;
		System.arraycopy(effectivePayoadPacked, 0, pdu, i, effectivePayoadPacked.length);
	}

	public int getSMSCDescriptorLength() {
		return smsDescriptorLength;
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
}
