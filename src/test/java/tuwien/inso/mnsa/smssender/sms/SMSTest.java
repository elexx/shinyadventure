package tuwien.inso.mnsa.smssender.sms;

import static org.junit.Assert.*;

import org.junit.Test;

import tuwien.inso.mnsa.smssender.Utils;

public class SMSTest {

	@Test
	public void test() throws SMSException {
		// incomplete
		byte[] x = Utils.hexToBytes("C4F01C949ED3");
		byte[] y = SMS.shiftRight(x, 6);

		System.out.println(Utils.bytesToHex(y));
	}

}
