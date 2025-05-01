package per.iiimabbie.discordbotfelix.util;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ConversationContext implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;


  private final List<Message> messages = new ArrayList<>();
  private static final int MAX_CONTEXT_SIZE = 20;

  /**
   * @param userId   新增
   * @param userName 新增
   */
  // 內部消息類，加入用戶ID和用戶名
  public record Message(String role, String content, String userId, String userName) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
  }


  // 添加用戶消息
  public void addUserMessage(String content, String userId, String userName) {
    messages.add(new Message("user", content, userId, userName));
    trimContextIfNeeded();
  }

  // 添加 AI 回覆
  public void addAiResponse(String content) {
    messages.add(new Message("model", content, "bot", "Felix"));
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
