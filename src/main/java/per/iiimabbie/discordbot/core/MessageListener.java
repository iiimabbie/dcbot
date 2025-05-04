package per.iiimabbie.discordbot.core;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import per.iiimabbie.discordbot.service.AiService;
import per.iiimabbie.discordbot.service.GeminiService;
import per.iiimabbie.discordbot.util.ConfigLoader;
import per.iiimabbie.discordbot.util.MessageUtils;

import java.util.concurrent.CompletableFuture;

/**
 * 消息監聽器，負責處理所有 Discord 訊息事件。
 * 這個類主要關注用戶對機器人的提及，並透過 AI 服務生成回應。
 *
 * @author iiimabbie
 */
public class MessageListener extends ListenerAdapter {

  private static final Logger logger = LoggerFactory.getLogger(MessageListener.class);
  private final AiService aiService;

  /**
   * 初始化消息監聽器
   */
  public MessageListener() {
    // 初始化 AI 服務
    String apiKey = ConfigLoader.get("gemini.api.key");
    if (apiKey == null || apiKey.isEmpty()) {
      throw new RuntimeException("Gemini API Key 未設定");
    }
    this.aiService = new GeminiService(apiKey);
    logger.info("消息監聽器初始化完成");
  }

  @Override
  public void onMessageReceived(MessageReceivedEvent event) {
    // 忽略機器人消息
    if (event.getAuthor().isBot()) {
      return;
    }

    Message message = event.getMessage();
    User selfUser = event.getJDA().getSelfUser();

    // 檢查是否有@mention機器人
    if (message.getMentions().isMentioned(selfUser)) {
      handleMention(event);
    }
  }

  /**
   * 處理特定的提及機器人的消息。
   * 從消息中提取內容，排除提及部分，然後使用 AI 服務生成回應。
   *
   * @param event 包含提及機器人的消息事件
   */
  private void handleMention(MessageReceivedEvent event) {
    Message message = event.getMessage();
    User selfUser = event.getJDA().getSelfUser();

    // 提取提及後的消息內容
    String content = message.getContentRaw()
        .replaceAll("<@!?" + selfUser.getId() + ">", "").trim();

    // 如果僅僅是@了機器人但沒有內容，可以給出提示
    if (content.isEmpty()) {
      message.reply("?").queue();
      return;
    }

    logger.info("收到@提及，用戶: {}, 消息: {}", event.getAuthor().getName(), content);

    // 添加loading表情
    CompletableFuture<Void> reactionFuture = MessageUtils.addLoadingReaction(message);

    // 獲取聊天歷史並生成回應
    aiService.generateResponse(
        MessageUtils.getChannelHistory(event, content, 20)
    ).thenAccept(answer -> {
      logger.info("AI回復: {}", answer);
      // 回覆可能超長的消息
      MessageUtils.replyMessage(message, answer, reactionFuture);
    }).exceptionally(ex -> {
      // 錯誤處理
      logger.error("AI 回應產生失敗", ex);
      MessageUtils.handleError(message, reactionFuture);

      // 回覆錯誤訊息
      message.reply("抱歉，處理您的請求時發生錯誤。請稍後再試。").queue();
      return null;
    });
  }
}