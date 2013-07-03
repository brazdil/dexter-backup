package uk.ac.cam.db538.dexter.dex;

public class InstrumentationException extends RuntimeException {

	private static final long serialVersionUID = -4541593095692838694L;

	public InstrumentationException() {
	}

	public InstrumentationException(String message) {
		super(message);
	}

	public InstrumentationException(Throwable cause) {
		super(cause);
	}

	public InstrumentationException(String message, Throwable cause) {
		super(message, cause);
	}

	public InstrumentationException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
