package tuwien.inso.mnsa.smssender.sms;

import java.util.Arrays;
import java.util.Random;

import tuwien.inso.mnsa.smssender.sms.SMS.SMSType;
import tuwien.inso.mnsa.smssender.translator.GSM0338Encoder;
import tuwien.inso.mnsa.smssender.translator.Interleaved7BitTranslator;
import tuwien.inso.mnsa.smssender.translator.MappingException;

public class SMSGenerator {
	private static final Random random = new Random();

	private static final int MAXIMUM_OCTET_LENGTH = 160;
	private static final int UDH_LENGTH = 6;
	private static final int OCTET_SPLIT = MAXIMUM_OCTET_LENGTH - Interleaved7BitTranslator.countUnpackedOctetsFloor(UDH_LENGTH) - 2; // 1 for the necessary padding, 1 extra octet is needed - not sure why :-(

	private SMSGenerator() {
	}

	public static boolean requiresSplit(String text) throws MappingException {
		byte[] octets = GSM0338Encoder.encodeToOctets(text);
		return octets.length > MAXIMUM_OCTET_LENGTH;
	}

	public static SMS generateSimpleSMS(GSMNumber smsCenter, SMSType type, GSMNumber destination, byte referenceId, String text) throws MappingException, SMSException {
		byte[] octets = GSM0338Encoder.encodeToOctets(text);
		if (octets.length <= MAXIMUM_OCTET_LENGTH)
			return new SMS(smsCenter, type, false, referenceId, destination, null, octets);
		else
			throw new SMSException("message too long (" + octets.length + " octets > " + MAXIMUM_OCTET_LENGTH + ") for a simple SMS, split required");
	}

	public static SMS[] generateConcatenatedSMS(GSMNumber smsCenter, SMSType type, GSMNumber destination, byte referenceId, String text) throws MappingException, SMSException {
		byte[] octets = GSM0338Encoder.encodeToOctets(text);

		byte smsId = (byte) (random.nextInt(0xFF));
		SMS[] ret = new SMS[octets.length / OCTET_SPLIT + (octets.length % OCTET_SPLIT == 0 ? 0 : 1)];
		for (int offset = 0, i = 0; offset < octets.length; offset += OCTET_SPLIT, i++) {
			byte[] udh = buildUdh(smsId, i + 1, ret.length);
			byte[] subarray = Arrays.copyOfRange(octets, offset, Math.min(offset + OCTET_SPLIT, octets.length));
			ret[i] = new SMS(smsCenter, type, true, referenceId, destination, udh, subarray);
		}

		return ret;
	}

	private static byte[] buildUdh(byte smsId, int part, int partCount) {
		byte[] udh = new byte[UDH_LENGTH];
		udh[0] = 5; // udh length
		udh[1] = 0; // udh type
		udh[2] = 3; // udh length excluding header (for whatever reason)
		udh[3] = smsId;
		udh[4] = (byte) partCount;
		udh[5] = (byte) part;
		return udh;
	}
}
