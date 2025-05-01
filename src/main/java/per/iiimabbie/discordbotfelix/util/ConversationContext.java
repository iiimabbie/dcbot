package per.iiimabbie.discordbotfelix.util;

import java.util.ArrayList;
import java.util.List;

public class ConversationContext {
  private final List<Message> messages = new ArrayList<>();
  private static final int MAX_CONTEXT_SIZE = 10; // 保留最近的10條消息

  // 內部消息類
    public record Message(String role, String content) {

  }

  // 添加用戶消息
  public void addUserMessage(String content) {
    messages.add(new Message("user", content));
    trimContextIfNeeded();
  }

  // 添加 AI 回覆
  public void addAiResponse(String content) {
    messages.add(new Message("model", content));
    trimContextIfNeeded();
  }

  // 獲取所有消息
  public List<Message> getMessages() {
    return new ArrayList<>(messages);
  }

  // 如果上下文太長，刪除最舊的消息
  private void trimContextIfNeeded() {
    while (messages.size() > MAX_CONTEXT_SIZE) {
      messages.removeFirst();
    }
  }
}
