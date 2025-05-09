package per.iiimabbie.discordbot.enums;

/**
 * 錯誤訊息枚舉
 * <p>
 * 定義系統中使用的標準化錯誤訊息，包含標題和描述信息。
 * 設計用於在嵌入訊息中顯示錯誤信息，提供更豐富的錯誤展示。
 * </p>
 *
 * @author iiimabbie
 */
public enum ErrorEnums {
  // 通用錯誤
  DEFAULT("系統錯誤", "抱歉，我可能出錯了o(TヘTo)"),

  // 權限相關錯誤
  PERMISSION_DENIED("權限錯誤", "你沒有權限執行此操作"),

  // 輸入相關錯誤
  INVALID_INPUT("輸入錯誤", "輸入的參數無效，請重試"),

  // 命令相關錯誤
  REQUEST_NOT_FOUND("請求錯誤", "找不到請求，請重新使用命令"),

  // 幫助命令相關錯誤
  HELP_PAGE_NOT_FOUND("頁面錯誤", "找不到請求的幫助頁面");

  private final String title;
  private final String description;

  /**
   * 建構子 - 創建一個帶有標題和描述的錯誤
   *
   * @param title 錯誤標題
   * @param description 錯誤描述
   */
  ErrorEnums(String title, String description) {
    this.title = title;
    this.description = description;
  }

  /**
   * 獲取錯誤標題
   *
   * @return 錯誤標題
   */
  public String getTitle() {
    return title;
  }

  /**
   * 獲取錯誤描述
   *
   * @return 錯誤描述
   */
  public String getDescription() {
    return description;
  }

  /**
   * 使用參數格式化錯誤描述
   *
   * @param args 格式化參數
   * @return 格式化後的錯誤描述
   */
  public String formatDescription(Object... args) {
    return String.format(description, args);
  }
}