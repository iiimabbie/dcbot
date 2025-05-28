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

    // 步驟3: 異步處理 Gemini API
    CompletableFuture.supplyAsync(() -> {
      try {
        return geminiService.processMessage(event.getChannel(), event.getMessage());
      } catch (BotException e) {
        // 記錄具體錯誤，但返回用戶友好的訊息
        log.error("AI 處理失敗 - 錯誤類型: {}, 訊息: {}",
            e.getErrorType(), e.getMessage(), e);
        return e.getErrorType().getErrMessage(); // 統一使用 enum 的訊息
      } catch (Exception e) {
        // 未預期的錯誤
        log.error("處理訊息時發生未知錯誤", e);
        return BotException.ErrorType.UNKNOWN_ERROR.getErrMessage(); // 統一使用 enum
      }
    }).thenAccept(response -> {
      log.info("AI 回應訊息 - 內容: {}", response);

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
            // 發送失敗時也要清除反應並顯示錯誤
            errorReaction(userMessage, loadingEmoji);
          }
      );
    });
  }

  /**
   * 錯誤反應處理
   * 移除 loading emoji，添加錯誤 emoji
   */
  private void errorReaction(Message message, String loadingEmoji) {
    if (!loadingEmoji.isEmpty()) {
      message.removeReaction(Emoji.fromFormatted(loadingEmoji)).queue();
    }

    // 優先使用自定義錯誤 emoji，否則使用 Unicode
    String errorEmoji = emojiManager.getToolEmoji(Tool.ERROR);
    if (!errorEmoji.isEmpty()) {
      message.addReaction(Emoji.fromFormatted(errorEmoji)).queue();
    } else {
      message.addReaction(Emoji.fromUnicode("💀")).queue();
    }
  }
}