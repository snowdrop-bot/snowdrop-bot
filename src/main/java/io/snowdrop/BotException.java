package io.snowdrop;

public class BotException extends RuntimeException {

  private static final long serialVersionUID = -5330504689883844495L;

public BotException() {
  }

  public BotException(String message) {
    super(message);
  }

  public BotException(Throwable cause) {
    super(cause);
  }

  public BotException(String message, Throwable cause) {
    super(message, cause);
  }


  public static BotException launderThrowable(Throwable cause) {
    return new BotException(cause);
  }

  public static BotException launderThrowable(String message, Throwable cause) {
    return new BotException(message, cause);
  }

}
