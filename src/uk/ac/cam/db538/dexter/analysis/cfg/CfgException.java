package uk.ac.cam.db538.dexter.analysis.cfg;

public class CfgException extends RuntimeException {

  private static final long serialVersionUID = 7432326563888505479L;

  public CfgException() {
    super();
  }

  public CfgException(String message, Throwable cause,
                      boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  public CfgException(String message, Throwable cause) {
    super(message, cause);
  }

  public CfgException(String message) {
    super(message);
  }

  public CfgException(Throwable cause) {
    super(cause);
  }
}
