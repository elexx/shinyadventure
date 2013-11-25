package tuwien.inso.mnsa.smssender;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class UtilsTest {
	@Test
	public void selfTest() {
		String hexInput = "FedCA71337";

		byte[] inBytes = Utils.hexToBytes(hexInput);
		assertEquals("byte 0", (byte) 0xfe, inBytes[0]);
		assertEquals("byte 1", (byte) 0xdc, inBytes[1]);
		assertEquals("byte 2", (byte) 0xa7, inBytes[2]);
		assertEquals("byte 3", (byte) 0x13, inBytes[3]);
		assertEquals("byte 4", (byte) 0x37, inBytes[4]);

		String hexOutput = Utils.bytesToHex(inBytes);
		assertEquals("all", hexInput.toLowerCase(), hexOutput.toLowerCase());
	}
}
