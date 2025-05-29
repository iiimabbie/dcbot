package per.iiimabbie.dcbot.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.springframework.stereotype.Component;
import per.iiimabbie.dcbot.command.impl.CommandsCommand;
import per.iiimabbie.dcbot.command.impl.StatusCommand;
import per.iiimabbie.dcbot.exception.BotException;

/**
 * 按鈕互動監聽器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ButtonInteractionListener extends ListenerAdapter {

  private final CommandsCommand commandsCommand;
  private final StatusCommand statusCommand;

  @Override
  public void onButtonInteraction(ButtonInteractionEvent event) {
    String buttonId = event.getComponentId();
    log.info("收到按鈕點擊: {} - 用戶: {}", buttonId, event.getUser().getName());

    switch (buttonId) {
      case "show_commands" -> {
        try {
          // 直接調用 CommandsCommand 的功能
          event.replyEmbeds(commandsCommand.createCommandsEmbed())
              .setEphemeral(true)
              .queue();
        } catch (BotException e) {
          // 處理已知的機器人異常
          log.error("執行按鈕命令 {} 失敗 - 錯誤類型: {}, 訊息: {}",
              buttonId, e.getErrorType(), e.getMessage(), e);
          if (!event.isAcknowledged()) {
            event.reply("❌ " + e.getErrorType().getErrMessage())
                .setEphemeral(true)
                .queue();
          }
        } catch (Exception e) {
          // 處理未知異常
          log.error("執行按鈕命令 {} 時發生未知錯誤", buttonId, e);

          if (!event.isAcknowledged()) {
            event.reply("❌ " + BotException.ErrorType.UNKNOWN_ERROR.getErrMessage())
                .setEphemeral(true)
                .queue();
          }
        }
      }
      case "refresh_status" -> {
        try {
          // 通知用戶正在更新
          event.deferEdit().queue();

          // 重新整理並顯示最新狀態
          Button refreshButton = Button.secondary("refresh_status", "🔄 重新整理");

          // 使用 StatusCommand 的方法獲取最新狀態
          event.getHook().editOriginalEmbeds(statusCommand.createStatusEmbed(event.getJDA()))
              .setActionRow(refreshButton)
              .queue();
        } catch (Exception e) {
          log.error("重新整理狀態時發生錯誤", e);
          event.reply("❌ 無法重新整理狀態資訊")
              .setEphemeral(true)
              .queue();
        }
      }
      default -> {
        log.warn("未處理的按鈕互動: {}", buttonId);
        event.reply("❌ 按鈕功能暫時無法使用")
            .setEphemeral(true)
            .queue();
      }
    }
  }
}