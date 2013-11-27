package tuwien.inso.mnsa.smssender.translator;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import tuwien.inso.mnsa.smssender.Utils;
import tuwien.inso.mnsa.smssender.translator.Interleaved7BitTranslator;

@RunWith(value = Parameterized.class)
public class TranslatorTest {

	private Interleaved7BitTranslator translator;
	private final byte[] rawMessage;
	private final byte[] encodedMessage;

	public TranslatorTest(byte[] rawMessage, byte[] encodedMessage) {
		this.rawMessage = rawMessage;
		this.encodedMessage = encodedMessage;
	}

	@Parameters
	public static Collection<Object[]> data() {
		Collection<Object[]> parameters = new LinkedList<>();
		parameters.add(new Object[] { "short".getBytes(), Utils.hexToBytes("73F45B4E07") });
		parameters.add(new Object[] { "1234567".getBytes(), Utils.hexToBytes("31D98C56B3DD00") });
		parameters.add(new Object[] { "12345678".getBytes(), Utils.hexToBytes("31D98C56B3DD70") });
		parameters.add(new Object[] { "thisoneislonger".getBytes(), Utils.hexToBytes("74747AFE7697D373F6DB7D2ECB01") });
		parameters.add(new Object[] { "mixed0934-=+TESTer#".getBytes(), Utils.hexToBytes("ED34BE4C86E566B4566F452D4EA965F908") });
		return parameters;
	}

	@Before
	public void startup() {
		translator = new Interleaved7BitTranslator();
	}

	@Test
	public void encodeTest() {
		byte[] encoded = translator.encode(rawMessage);

		assertThat(Arrays.asList(encoded), contains(encodedMessage));
	}

	@Test
	public void decoderTest() {
		byte[] decoded = translator.decode(encodedMessage);

		// if the encoded size is a multiple of 7 there is a trailing 0x00 which may or may not be part of the original byte sequence
		byte[] rawMessageWithTrailingNull = Arrays.copyOf(rawMessage, rawMessage.length + 1);

		assertThat(Arrays.asList(decoded), anyOf(contains(rawMessage), contains(rawMessageWithTrailingNull)));
	}

}
