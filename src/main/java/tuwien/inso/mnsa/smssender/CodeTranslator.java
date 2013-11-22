package tuwien.inso.mnsa.smssender;

public interface CodeTranslator {

	byte[] encode(byte[] input);

	byte[] decode(byte[] input);
}
