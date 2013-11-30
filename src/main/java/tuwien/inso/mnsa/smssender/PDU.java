package tuwien.inso.mnsa.smssender;

import tuwien.inso.mnsa.smssender.translator.GSM0338Encoder;
import tuwien.inso.mnsa.smssender.translator.MappingException;

public class PDU {

	//	private static final short REPLY_PATH = 0x1 << 7;
	//	private static final short USER_DATA_HEADER_INDICATOR = 0x1 << 6;
	//	private static final short STATUS_REPORT_REQUEST = 0x1 << 5;
	//	private static final short VALIDITY_PERIOD_FORMAT = 0x3 << 3;
	//	private static final short REJECT_DUPLICATES = 0x1 << 2;
	//	private static final short MESSAGE_TYPE_INDICATOR = 0x3 << 0;

	private final byte smsc;
	private final byte firstOctet;
	private final byte messegeReference;

	private final byte phoneNumberLength;
	private final byte typeOfAddress;
	private final byte[] phoneNumber;
	private final byte protocolIdentifier;
	private final byte dataEncodingScheme;

	private final byte messageLength;
	private final byte[] message;

	/**
	 * @param phoneNumber
	 *            the number itself. no spaces, leading zeros or "+" allowed. eg 4366012312312
	 * @param message
	 *            only up to 140 characters are supported.
	 * @throws PDUException
	 */
	public PDU(String phoneNumber, String message) throws PDUException {
		smsc = 0x00; // uses default phone smsc number
		firstOctet = 0x01;
		messegeReference = 0x00; // let the phone set the reference number
		typeOfAddress = (byte) 0x91; // international phone number

		phoneNumberLength = (byte) phoneNumber.length();
		if (phoneNumber.length() % 2 != 0) {
			phoneNumber += "F";
		}

		this.phoneNumber = Utils.hexToBytes(phoneNumber);
		for (int i = 0; i < this.phoneNumber.length; i ++) {
			this.phoneNumber[i] = (byte) (this.phoneNumber[i] >>> 4 | this.phoneNumber[i] << 4);
		}

		protocolIdentifier = 0x00;
		dataEncodingScheme = 0x00; // 7bit

		messageLength = (byte) message.length();
		try {
			this.message = GSM0338Encoder.encode(message);
		} catch (MappingException e) {
			throw new PDUException(e);
		}
	}

	public String toHexString() {
		StringBuilder builder = new StringBuilder();
		builder.append(Utils.bytesToHex(smsc));
		builder.append(Utils.bytesToHex(firstOctet));
		builder.append(Utils.bytesToHex(messegeReference));
		builder.append(Utils.bytesToHex(phoneNumberLength));
		builder.append(Utils.bytesToHex(typeOfAddress));
		builder.append(Utils.bytesToHex(phoneNumber));
		builder.append(Utils.bytesToHex(protocolIdentifier));
		builder.append(Utils.bytesToHex(dataEncodingScheme));
		builder.append(Utils.bytesToHex(messageLength));
		builder.append(Utils.bytesToHex(message));

		return builder.toString();
	}


}
