package tuwien.inso.mnsa.smssender.translator;

public interface CodeTranslator {

	byte[] encode(byte[] input);

	byte[] decode(byte[] input);
}
