package tuwien.inso.mnsa.smssender;

/*
 * How encoder and decoder are working:
 * 0       1       2       3       4       5       6       7       8       9       10      11      12      13      14      15
 * ++++++++--------++++++++--------++++++++--------++++++++--------++++++++--------++++++++--------++++++++--------++++++++--------
 *  6543210 6543210 6543210 6543210 6543210 6543210 6543210 6543210 6543210 6543210 6543210 6543210 6543210 6543210 6543210 6543210
 * 65432106543210654321065432106543210654321065432106543210        65432106543210654321065432106543210654321065432106543210
 * ++++++++--------++++++++--------++++++++--------++++++++        --------++++++++--------++++++++--------++++++++--------++++++++
 * 0       1       2       3       4       5       6               7       8       9       10      11      12      13      14
 *
 *
 * ENCODER
 * output[0] = input[0]  << 1 | input[1]  >>> 6
 * output[1] = input[1]  << 2 | input[2]  >>> 5
 * output[2] = input[2]  << 3 | input[3]  >>> 4
 * output[3] = input[3]  << 4 | input[4]  >>> 3
 * output[4] = input[4]  << 5 | input[5]  >>> 2
 * output[5] = input[5]  << 6 | input[6]  >>> 1
 * output[6] = input[6]  << 7 | input[7]  >>> 0
 * output[7] = input[8]  << 1 | input[9]  >>> 6
 * output[8] = input[9]  << 2 | input[10] >>> 5
 * output[9] = input[10] << 3 | input[11] >>> 4
 * ....
 * 
 * 
 * DECODER
 * output[0] = (input[0] >>> 1                ) & 0x7f
 * output[1] = (input[1] >>> 2 | input[0] << 6) & 0x7f
 * output[2] = (input[2] >>> 3 | input[1] << 5) & 0x7f
 * output[3] = (input[3] >>> 4 | input[2] << 4) & 0x7f
 * output[4] = (input[4] >>> 5 | input[3] << 3) & 0x7f
 * output[5] = (input[5] >>> 6 | input[4] << 2) & 0x7f
 * output[6] = (input[6] >>> 7 | input[5] << 1) & 0x7f
 * output[7] = (                 input[6] << 0) & 0x7f
 * output[8] = (input[7] >>> 1                ) & 0x7f
 * output[9] = (input[8] >>> 2 | input[7] << 6) & 0x7f
 * ....
 *
 */

public class Ascii7BitTranslator implements CodeTranslator {

	@Override
	public byte[] encode(byte[] input) {
		byte[] output = new byte[(int) Math.ceil(input.length / 8f * 7)];

		int ii;
		for (int oi = 0, shift = 1; oi < output.length; oi++, shift = shift % 7 + 1) {
			ii = oi * 8 / 7;
			output[oi] = (byte) (input[ii] << shift);
			if (ii + 1 < input.length)
				output[oi] |= (input[ii + 1] & 0xFF) >>> (7 - shift);
		}

		return output;
	}

	@Override
	public byte[] decode(byte[] input) {
		byte[] output = new byte[(int) Math.ceil(input.length / 7f * 8)];

		int oi;
		for (int ii = 0, shift = 1; ii < input.length; ii++, shift = shift % 7 + 1) {
			oi = ii * 8 / 7;

			output[oi] |= ((input[ii] & 0xFF) >>> shift) & 0x7F;
			output[oi + 1] = (byte) ((input[ii] << (7 - shift)) & 0x7F);
		}

		output[output.length - 1] &= 0x7F;

		return output;
	}

}
