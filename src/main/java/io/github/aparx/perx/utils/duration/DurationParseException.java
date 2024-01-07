package io.github.aparx.perx.utils.duration;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-07 07:44
 * @since 1.0
 */
public class DurationParseException extends Exception {

  public DurationParseException() {}

  public DurationParseException(String message) {
    super(message);
  }

  public DurationParseException(String message, Throwable cause) {
    super(message, cause);
  }

  public DurationParseException(Throwable cause) {
    super(cause);
  }

  public DurationParseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
