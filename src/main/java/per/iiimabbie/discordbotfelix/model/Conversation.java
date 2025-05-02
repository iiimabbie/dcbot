package per.iiimabbie.discordbotfelix.model;

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
    addMessage(new ChatMessage("model", content, "bot", "Felix"));
  }

  /**
   * 添加消息到對話
   */
  public void addMessage(ChatMessage message) {
    messages.add(0, message); // 新消息加到前面
    // 保持對話大小不超過限制
    if (messages.size() > maxSize) {
      messages.remove(messages.size() - 1);
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