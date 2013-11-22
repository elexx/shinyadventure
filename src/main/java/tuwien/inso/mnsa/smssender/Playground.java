package tuwien.inso.mnsa.smssender;

public class Playground {

	public static void main(String[] args) {
		byte answer1, answer2, answer3, answer4, answer5, answer6, answer7, answer8, answer9, answer10, answer11;
		answer1 = (byte) (0xAB >>> 1);
		// = 0x55

		byte valueByte = (byte) 0xAB;
		answer2 = (byte) (valueByte >>> 1);
		// = 0xD5

		int valueInt = 0xAB;
		answer3 = (byte) (valueInt >>> 1);
		// = 0x55

		int shiftByte = 1;
		answer4 = (byte) (0xAB >>> shiftByte);
		// = 0x55

		byte shfitInt = 1;
		answer5 = (byte) (0xAB >>> shfitInt);
		// = 0x55

		answer6 = (byte) (valueByte >>> shiftByte);
		// = 0xD5

		answer7 = (byte) (valueInt >>> shfitInt);
		// = 0x55

		answer8 = (byte) (valueByte >>> shfitInt);
		// = 0xD5

		answer9 = (byte) (valueInt >>> shiftByte);
		// = 0x55

		int newValueInt = valueByte;
		answer10 = (byte) (newValueInt >>> 1);
		// = 0xD5

		answer11 = (byte) ((valueByte & 0xFF) >>> 1);
		// = 0xD5
	}

}
