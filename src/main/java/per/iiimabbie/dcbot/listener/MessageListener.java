package per.iiimabbie.dcbot.listener;

import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;
import per.iiimabbie.dcbot.config.BotConfig;
import per.iiimabbie.dcbot.enums.BotEmojis.Tool;
import per.iiimabbie.dcbot.exception.BotException;
import per.iiimabbie.dcbot.service.EmojiManager;
import per.iiimabbie.dcbot.service.GeminiService;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageListener extends ListenerAdapter {

  private final EmojiManager emojiManager;
  private final GeminiService geminiService;

  @Override
  public void onMessageReceived(MessageReceivedEvent event) {
    // 忽略所有機器人（包括自己）
    if (event.getAuthor().isBot()) {
      return;
    }
    // 檢查是否需要處理這個訊息
    if (!shouldProcessMessage(event)) {
      return;
    }

    // 處理訊息的完整流程
    processMessageWithReactions(event);
  }

  /**
   * 判斷是否需要處理這個訊息
   * 條件：
   * 1. 直接 @ 機器人
   * 2. 在討論串中且機器人有參與過
   * 3. 私訊
   */
  private boolean shouldProcessMessage(MessageReceivedEvent event) {

    // 私訊直接處理
    if (!event.isFromGuild()) {
      return true;
    }

    // 檢查是否 @ 機器人
    if (event.getMessage().getMentions().isMentioned(event.getJDA().getSelfUser())) {
      return true;
    }

    // 如果在討論串中，直接處理（機器人能收到訊息就代表它在這個討論串裡）
    return event.getChannel().getType() == ChannelType.GUILD_PUBLIC_THREAD ||
        event.getChannel().getType() == ChannelType.GUILD_PRIVATE_THREAD;
  }

  /**
   * 完整的訊息處理流程：反應 -> 打字中 -> 處理 -> 回覆 -> 清除反應
   */
  private void processMessageWithReactions(MessageReceivedEvent event) {

    /* String rawMessage = event.getMessage().getContentRaw(); // "<@1367355249643360317> 收"
    String cleanMessage = event.getMessage().getContentDisplay(); // "@bot名稱 收"
    String strippedMessage = rawMessage.replaceAll("<@!?\\d+>\\s*", ""); // "收" */

    Message userMessage = event.getMessage();
    String username = event.getAuthor().getName();
    String channelName = event.getChannel().getName();

    log.info("處理訊息 - 用戶: {}, 頻道: {}, 內容: {}",
        username, channelName, userMessage.getContentDisplay());

    // 步驟1: 先對用戶訊息按個 emoji 表示收到了
    String loadingEmoji = emojiManager.getToolEmoji(Tool.LOADING);
    if (!loadingEmoji.isEmpty()) {
      userMessage.addReaction(Emoji.fromFormatted(loadingEmoji)).queue();
    }

    // 步驟2: 顯示打字中狀態
    event.getChannel().sendTyping().queue();

    // 步驟3: GeminiAPI
    CompletableFuture.supplyAsync(() -> {
      try {
        return geminiService.processMessage(event.getChannel(), event.getMessage());
      } catch (BotException e) {
        // 記錄具體錯誤，但返回用戶友好的訊息
        log.error("AI 處理失敗 - 錯誤類型: {}, 訊息: {}",
            e.getErrorType(), e.getMessage(), e);
        return getErrorMessage(e.getErrorType());
      } catch (Exception e) {
        // 未預期的錯誤
        log.error("處理訊息時發生未知錯誤", e);
        return getErrorMessage(BotException.ErrorType.UNKNOWN_ERROR);
      }
    }).thenAccept(response -> {
      log.info("AI 回應訊息 - 內容: {}",
          response);
      // 步驟4: 發送回覆
      event.getChannel().sendMessage(response).queue(
          sentMessage -> {
            // 步驟5: 回覆成功後清除處理中的反應
            if (!loadingEmoji.isEmpty()) {
              userMessage.removeReaction(Emoji.fromFormatted(loadingEmoji)).queue();
            }
          },
          error -> {
            log.error("發送回覆失敗", error);
            // 發送失敗時也要清除反應
            errorReaction(userMessage, loadingEmoji);
          }
      );
    });
  }

  /**
   * 根據錯誤類型返回對應的用戶友好訊息
   */
  private String getErrorMessage(BotException.ErrorType errorType) {
    return switch (errorType) {
      case GEMINI_API_ERROR -> "我的大腦暫時短路了，請稍後再試試";
      case DISCORD_API_ERROR -> "Discord 好像壞掉了呢害我拿不到資料T_T";
      case NETWORK_ERROR -> "網路好像有點問題，等會兒再找我聊天吧";
      case CONFIGURATION_ERROR -> "我的設定好像有問題，請聯繫管理員";
      case UNKNOWN_ERROR -> "發生了一些意外狀況，我正在努力修復中";
    };
  }

  /**
   * 錯誤反應 - 移除 loading /添加 death
   */
  private void errorReaction(Message message, String loadingEmoji) {
    if (!loadingEmoji.isEmpty()) {
      message.removeReaction(Emoji.fromFormatted(loadingEmoji)).queue();
    }
    message.addReaction(Emoji.fromUnicode("💀")).queue();
  }
}