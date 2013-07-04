package uk.ac.cam.db538.dexter.hierarchy;

public class HierarchyException extends RuntimeException {

	private static final long serialVersionUID = -6823798615698799517L;

	public HierarchyException() {
	}

	public HierarchyException(String message) {
		super(message);
	}

	public HierarchyException(Throwable cause) {
		super(cause);
	}

	public HierarchyException(String message, Throwable cause) {
		super(message, cause);
	}

	public HierarchyException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
