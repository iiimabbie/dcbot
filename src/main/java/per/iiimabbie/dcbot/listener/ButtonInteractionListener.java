package per.iiimabbie.dcbot.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;
import per.iiimabbie.dcbot.command.impl.CommandsCommand;
import per.iiimabbie.dcbot.exception.BotException;

/**
 * 按鈕互動監聽器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ButtonInteractionListener extends ListenerAdapter {

  private final CommandsCommand commandsCommand;

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
        // 重新整理狀態 - 這裡需要重新建立 SlashCommandInteractionEvent
        // 或者直接回應新的狀態資訊
        event.reply("🔄 狀態已重新整理！使用 `/status` 查看最新狀態")
            .setEphemeral(true)
            .queue();
      }
      case "quick_ping" -> {
        long gatewayPing = event.getJDA().getGatewayPing();
        event.reply(String.format("🏓 當前延遲: %d ms", gatewayPing))
            .setEphemeral(true)
            .queue();
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