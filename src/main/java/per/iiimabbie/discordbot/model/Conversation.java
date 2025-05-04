package per.iiimabbie.discordbot.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 表示單個對話上下文，包含消息歷史
 */
public class Conversation {
  private final String channelId;
  private final List<ChatMessage> messages;
  private final int maxSize;

  public Conversation(String channelId) {
    this(channelId, 20);
  }

  public Conversation(String channelId, int maxSize) {
    this.channelId = channelId;
    this.messages = new ArrayList<>();
    this.maxSize = maxSize;
  }

  /**
   * 添加用戶消息到對話
   */
  public void addUserMessage(String content, String userId, String userName) {
    addMessage(new ChatMessage("user", content, userId, userName));
  }

  /**
   * 添加機器人消息到對話
   */
  public void addBotMessage(String content) {
    addMessage(new ChatMessage("model", content, "bot", null));
  }

  /**
   * 添加消息到對話歷史。
   * 新消息會添加到歷史的前面，如果消息數量超過設定的最大值，
   * 最舊的消息會被移除。
   *
   * @param message 要添加的聊天消息
   * @throws NullPointerException 如果消息為 null
   */
  public void addMessage(ChatMessage message) {
    messages.addFirst(message); // 新消息加到前面
    // 保持對話大小不超過限制
    if (messages.size() > maxSize) {
      messages.removeLast();
    }
  }

  /**
   * 清空對話
   */
  public void clear() {
    messages.clear();
  }

  /**
   * 獲取全部消息
   */
  public List<ChatMessage> getMessages() {
    return new ArrayList<>(messages);
  }

  /**
   * 獲取頻道ID
   */
  public String getChannelId() {
    return channelId;
  }
}