package tuwien.inso.mnsa.smssender;

public class SMSSender {

	public static void main(String[] args) {
		CodeTranslator smsTranslator = new GSM0338Translator();

		String message = "oh my fucking god this one was complicated";
		System.out.println(Utils.bytesToHex(message.getBytes()));

		byte[] encodedMessage = smsTranslator.encode(message.getBytes());

		System.out.println(Utils.bytesToHex(encodedMessage));
	}


}
