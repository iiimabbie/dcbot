package per.iiimabbie.dcbot.exception;

import lombok.Getter;

/**
 * Discord 機器人業務異常
 */
@Getter
public class BotException extends RuntimeException {

  private final ErrorType errorType;

  public BotException(ErrorType errorType, String message) {
    super(message);
    this.errorType = errorType;
  }

  public BotException(ErrorType errorType, String message, Throwable cause) {
    super(message, cause);
    this.errorType = errorType;
  }

  public enum ErrorType {
    GEMINI_API_ERROR,
    DISCORD_API_ERROR,
    NETWORK_ERROR,
    CONFIGURATION_ERROR,
    UNKNOWN_ERROR
  }
}