package uk.ac.cam.db538.dexter.dex;

public class DexParsingException extends Exception {
	private static final long serialVersionUID = 32523895693284562L;

	public DexParsingException() {
		super();
	}

	public DexParsingException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public DexParsingException(String message, Throwable cause) {
		super(message, cause);
	}

	public DexParsingException(String message) {
		super(message);
	}

	public DexParsingException(Throwable cause) {
		super(cause);
	}
}