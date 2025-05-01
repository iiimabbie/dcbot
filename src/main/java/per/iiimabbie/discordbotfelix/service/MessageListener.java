package per.iiimabbie.discordbotfelix.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import per.iiimabbie.discordbotfelix.util.ConversationContext;

public class MessageListener extends ListenerAdapter {
  private static final Logger logger = LoggerFactory.getLogger(MessageListener.class);

  private final GeminiService geminiService;
  private final String prefix;
  private final Map<String, ConversationContext> userContexts = new HashMap<>();
  private final String loadingEmoji = "⏳"; // 加載表情符號

  public MessageListener() {
    // 讀取配置檔
    Properties prop = new Properties();
    try {
      prop.load(new FileInputStream("config.properties"));
    } catch (IOException e) {
      logger.error("無法讀取配置檔: {}", e.getMessage());
      throw new RuntimeException("配置檔讀取失敗", e);
    }

    // 初始化 Gemini 服務
    String geminiApiKey = prop.getProperty("gemini.api.key");
    if (geminiApiKey == null || geminiApiKey.isEmpty()) {
      throw new RuntimeException("Gemini API Key 未設定");
    }

    this.geminiService = new GeminiService(geminiApiKey);
    this.prefix = prop.getProperty("bot.command.prefix", "!");
  }

  @Override
  public void onMessageReceived(MessageReceivedEvent event) {
    // 忽略機器人消息
    if (event.getAuthor().isBot()) {
      return;
    }

    Message message = event.getMessage();
    String content = message.getContentRaw();
    String userId = event.getAuthor().getId();

    // 檢查是否是命令
    if (content.startsWith(prefix + "ai ")) {
      String query = content.substring((prefix + "ai ").length());

      // 添加 loading 表情符號
      CompletableFuture<Void> reactionFuture = message.addReaction(Emoji.fromUnicode(loadingEmoji)).submit();

      // 獲取用戶的對話上下文，如果沒有則創建新的
      ConversationContext context = userContexts.getOrDefault(userId, new ConversationContext());

      // 添加用戶的新消息到上下文
      context.addUserMessage(query);

      // 呼叫 Gemini API 並傳遞上下文
      geminiService.generateResponseWithContext(context).thenAccept(answer -> {
        // 添加 AI 回覆到上下文
        context.addAiResponse(answer);

        // 更新用戶的對話上下文
        userContexts.put(userId, context);

        // 檢查回應長度，Discord 有 2000 字元限制
        if (answer.length() <= 2000) {
          message.reply(answer).queue(response -> {
            // 消息發送後，移除 loading 表情符號
            reactionFuture.thenRun(() -> message.removeReaction(Emoji.fromUnicode(loadingEmoji)).queue());
          });
        } else {
          // 分段發送
          message.reply(answer.substring(0, 2000)).queue();

          // 計算需要多少段
          int parts = (int) Math.ceil(answer.length() / 2000.0);
          for (int i = 1; i < parts; i++) {
            int start = i * 2000;
            int end = Math.min(start + 2000, answer.length());
            String part = answer.substring(start, end);

            if (i == parts - 1) {
              // 最後一個部分，完成後移除表情符號
              event.getChannel().sendMessage(part).queue(response ->  reactionFuture.thenRun(() -> message.removeReaction(Emoji.fromUnicode(loadingEmoji)).queue()));
            } else {
              event.getChannel().sendMessage(part).queue();
            }
          }
        }
      }).exceptionally(ex -> {
        // 發生錯誤，回覆錯誤訊息並移除表情符號
        message.reply("處理請求時發生錯誤: " + ex.getMessage()).queue();
        reactionFuture.thenRun(() -> message.removeReaction(Emoji.fromUnicode(loadingEmoji)).queue());
        return null;
      });
    } else if (content.equals(prefix + "reset")) {
      // 重置對話上下文
      userContexts.remove(userId);
      message.reply("已重置你的對話上下文！").queue();
    }
  }
}
