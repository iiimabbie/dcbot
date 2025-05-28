package per.iiimabbie.dcbot.exception;

import lombok.Getter;

/**
 * Discord 機器人業務異常
 * 統一管理所有機器人相關的錯誤類型和用戶友好訊息
 *
 * @author iiimabbie
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

  /**
   * 錯誤類型枚舉
   * 每個錯誤類型都有對應的用戶友好訊息
   */
  @Getter
  public enum ErrorType {
    // AI 相關錯誤
    GEMINI_API_ERROR("我的大腦暫時短路了，請稍後再試試"),

    // Discord API 相關錯誤
    DISCORD_API_ERROR("Discord 好像壞掉了呢害我拿不到資料T_T"),

    // 網路相關錯誤
    NETWORK_ERROR("網路好像有點問題，等會兒再找我聊天吧"),

    // 配置相關錯誤
    CONFIGURATION_ERROR("我的設定好像有問題，請聯繫管理員"),

    // 指令相關錯誤
    COMMAND_EXECUTION_ERROR("指令執行時出了點小狀況"),
    COMMAND_NOT_FOUND("找不到這個指令呢"),

    // 權限相關錯誤
    PERMISSION_DENIED("你沒有權限執行這個操作"),

    // 參數相關錯誤
    INVALID_PARAMETER("參數格式不正確"),

    // 通用錯誤
    UNKNOWN_ERROR("發生了一些意外狀況，我正在努力修復中");

    private final String errMessage;

    ErrorType(String errMessage) {
      this.errMessage = errMessage;
    }
  }

  /**
   * 便利方法：快速創建常用異常
   */
  public static BotException geminiError(String details) {
    return new BotException(ErrorType.GEMINI_API_ERROR, details);
  }

  public static BotException geminiError(String details, Throwable cause) {
    return new BotException(ErrorType.GEMINI_API_ERROR, details, cause);
  }

  public static BotException discordError(String details) {
    return new BotException(ErrorType.DISCORD_API_ERROR, details);
  }

  public static BotException discordError(String details, Throwable cause) {
    return new BotException(ErrorType.DISCORD_API_ERROR, details, cause);
  }

  public static BotException networkError(String details) {
    return new BotException(ErrorType.NETWORK_ERROR, details);
  }

  public static BotException networkError(String details, Throwable cause) {
    return new BotException(ErrorType.NETWORK_ERROR, details, cause);
  }

  public static BotException unknownError(String details) {
    return new BotException(ErrorType.UNKNOWN_ERROR, details);
  }

  public static BotException unknownError(String details, Throwable cause) {
    return new BotException(ErrorType.UNKNOWN_ERROR, details, cause);
  }
}