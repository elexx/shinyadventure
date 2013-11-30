package tuwien.inso.mnsa.smssender;

public class PDUException extends Exception {
	private static final long serialVersionUID = -7144849783633006084L;

	public PDUException() {
		super();
	}

	public PDUException(String message) {
		super(message);
	}

	public PDUException(String message, Throwable cause) {
		super(message, cause);
	}

	public PDUException(Throwable cause) {
		super(cause);
	}
}
