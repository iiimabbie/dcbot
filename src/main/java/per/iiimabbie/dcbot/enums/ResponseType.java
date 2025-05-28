package per.iiimabbie.dcbot.enums;

/**
 * Slash Command 回應類型
 */
public enum ResponseType {
  // 立即回應
  REPLY,              // 公開回應
  REPLY_EPHEMERAL,    // 私人回應

  // 延遲回應 (用於需要時間處理的請求)
  DEFER_REPLY,        // 延遲公開回應
  DEFER_REPLY_EPHEMERAL,  // 延遲私人回應

  // 更新訊息
  EDIT_ORIGINAL,      // 編輯原始回應
  FOLLOWUP,           // 後續訊息
  FOLLOWUP_EPHEMERAL  // 後續私人訊息
}