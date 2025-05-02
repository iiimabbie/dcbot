package per.iiimabbie.discordbotfelix.util;

import java.util.Collection;
import java.util.Collections;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import per.iiimabbie.discordbotfelix.model.ChatMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 消息處理工具類
 */
public class MessageUtils {
  private static final Logger logger = LoggerFactory.getLogger(MessageUtils.class);
  private static final String LOADING_EMOJI = "⏳";
  private static final String ERROR_EMOJI = "💀";
  private static final int MAX_CONTEXT_SIZE = 20; // 最多取用的歷史消息數量

  /**
   * 添加loading表情
   */
  public static CompletableFuture<Void> addLoadingReaction(Message message) {
    return message.addReaction(Emoji.fromUnicode(LOADING_EMOJI)).submit();
  }

  /**
   * 處理錯誤
   */
  public static void handleError(Message message, CompletableFuture<Void> reactionFuture) {
    reactionFuture.thenRun(() -> {
      message.removeReaction(Emoji.fromUnicode(LOADING_EMOJI)).queue();
      message.addReaction(Emoji.fromUnicode(ERROR_EMOJI)).queue();
    });
  }

  /**
   * 回覆可能超長的消息
   */
  public static void replyMessage(Message originalMessage, String content, CompletableFuture<Void> reactionFuture) {
    // 檢查回應長度，Discord 有 2000 字元限制
    if (content.length() <= 2000) {
      originalMessage.reply(content).queue(response -> {
        // 消息發送後，移除 loading 表情符號
        reactionFuture.thenRun(() ->
            originalMessage.removeReaction(Emoji.fromUnicode(LOADING_EMOJI)).queue());
      });
    } else {
      // 分段發送
      originalMessage.reply(content.substring(0, 2000)).queue();
      // 計算需要多少段
      int parts = (int) Math.ceil(content.length() / 2000.0);
      for (int i = 1; i < parts; i++) {
        int start = i * 2000;
        int end = Math.min(start + 2000, content.length());
        String part = content.substring(start, end);
        if (i == parts - 1) {
          // 最後一個部分，完成後移除表情符號
          originalMessage.getChannel().sendMessage(part).queue(response ->
              reactionFuture.thenRun(() ->
                  originalMessage.removeReaction(Emoji.fromUnicode(LOADING_EMOJI)).queue())
          );
        } else {
          originalMessage.getChannel().sendMessage(part).queue();
        }
      }
    }
  }

  /**
   * 獲取聊天歷史
   * @param event 消息事件
   * @param currentMessage 當前消息內容
   * @param limit 最大歷史數量
   * @return 對話歷史列表
   */
  public static List<ChatMessage> getChannelHistory(MessageReceivedEvent event, String currentMessage, int limit) {
    List<ChatMessage> chatHistory = new ArrayList<>();

    try {
      // 獲取頻道歷史訊息
      MessageHistory history = event.getChannel().getHistory();
      List<Message> messages = history.retrievePast(MAX_CONTEXT_SIZE).complete();
      // 整個表倒轉，因為dc取得歷史消息記錄是最新的在上面
      Collections.reverse(messages);

      // 過濾並轉換消息
      for (Message msg : messages) {
        // 跳過當前消息
        if (msg.getId().equals(event.getMessageId())) {
          continue;
        }

        // 轉換消息格式
        boolean isBot = msg.getAuthor().isBot();
        String role = isBot ? "model" : "user";
        String content = msg.getContentDisplay();
        String msgUserId = msg.getAuthor().getId();
        String msgUserName = msg.getAuthor().getName();

        // 加入歷史列表
        chatHistory.add(new ChatMessage(role, content, msgUserId, msgUserName));

        // 檢查是否已達到上下文大小限制
        if (chatHistory.size() >= limit) {
          break;
        }
      }

      // 當前用戶的消息後加入列表
      String userName = event.getAuthor().getName();
      String userId = event.getAuthor().getId();
      chatHistory.add(new ChatMessage("user", currentMessage, userId, userName));

      logger.debug("獲取了 {} 條歷史消息", chatHistory.size());

    } catch (Exception e) {
      logger.error("獲取頻道歷史失敗", e);
    }

    return chatHistory;
  }
}