package per.iiimabbie.dcbot.enums;

/**
 * Discord 訊息類型
 */
public enum MessageType {
  // 基本訊息
  TEXT,           // 純文字
  EMBED,          // 嵌入訊息
  EPHEMERAL,      // 短暫訊息 (只有發送者看到)

  // 互動訊息
  BUTTON,         // 帶按鈕
  SELECT_MENU,    // 帶選單
  MODAL,          // 彈出表單

  // 特殊用途
  FILE,           // 檔案訊息
  REPLY,          // 回覆訊息
  THREAD          // 討論串訊息
}