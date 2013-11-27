package tuwien.inso.mnsa.smssender.translator;

class Interleaved7BitTranslator {

	public byte[] encode(byte[] input) {
		byte[] output = new byte[(int) Math.ceil(input.length * 7f / 8)];

		int ii;
		for (int oi = 0; oi < output.length; oi++) {
			ii = oi * 8 / 7;
			output[oi] |= (input[ii] & 0xFF) >>> (oi % 7);
			if (ii + 1 < input.length)
				output[oi] |= (input[ii + 1] & 0x7F) << (7 - oi % 7);
		}

		return output;
	}

	public byte[] decode(byte[] input) {
		int outputlength = input.length * 8 / 7;
		byte[] output = new byte[outputlength];

		int oi;
		for (int ii = 0; ii < input.length; ii++) {
			oi = ii * 8 / 7;

			output[oi] |= ((input[ii] & 0xFF) << (ii % 7)) & 0x7F;
			if (oi + 1 < output.length)
				output[oi + 1] = (byte) ((input[ii] & 0xFF) >>> (7 - ii % 7));
		}

		return output;
	}

}
