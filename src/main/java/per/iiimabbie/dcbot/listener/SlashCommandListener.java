package per.iiimabbie.dcbot.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;
import per.iiimabbie.dcbot.command.SlashCommand;
import per.iiimabbie.dcbot.exception.BotException;
import per.iiimabbie.dcbot.service.CommandManager;

/**
 * Slash Command 事件監聽器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SlashCommandListener extends ListenerAdapter {

  private final CommandManager commandManager;

  @Override
  public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
    String commandName = event.getName();

    log.info("收到 Slash Command: {} - 用戶: {}", commandName, event.getUser().getName());

    SlashCommand command = commandManager.getCommand(commandName);
    if (command != null) {
      try {
        command.execute(event);
      } catch (BotException e) {
        log.error("執行指令 {} 失敗 - 錯誤類型: {}, 訊息: {}",
            commandName, e.getErrorType(), e.getMessage(), e);

        if (!event.isAcknowledged()) {
          event.reply("❌ " + e.getErrorType().getErrMessage())
              .setEphemeral(true)
              .queue();
        }
      } catch (Exception e) {
        log.error("執行指令 {} 時發生未知錯誤", commandName, e);

        if (!event.isAcknowledged()) {
          event.reply("❌ " + BotException.ErrorType.UNKNOWN_ERROR.getErrMessage())
              .setEphemeral(true)
              .queue();
        }
      }
    } else {
      log.warn("未找到指令: {}", commandName);
      event.reply("❌ 未知的指令")
          .setEphemeral(true)
          .queue();
    }
  }
}