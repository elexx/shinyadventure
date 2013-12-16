package tuwien.inso.mnsa.smssender.sms;

import static org.junit.Assert.*;

import org.junit.Test;

public class GSMNumberTest {

	@Test
	public void test() throws SMSException {
		// incomplete
		GSMNumber number = GSMNumber.fromInternational("+436641612733");
		number.getEncoded();
	}

}
