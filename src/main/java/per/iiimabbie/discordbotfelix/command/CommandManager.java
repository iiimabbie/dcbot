package per.iiimabbie.discordbotfelix.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 命令管理器
 */
public class CommandManager extends ListenerAdapter {

  private static final Logger logger = LoggerFactory.getLogger(CommandManager.class);
  private final Map<String, SlashCommand> commands = new HashMap<>();
  private final List<ButtonHandler> buttonHandlers = new ArrayList<>();

  /**
   * 註冊命令
   * @param command 要註冊的命令
   */
  public void registerCommand(SlashCommand command) {
    commands.put(command.getName(), command);

    // 如果命令也實現了按鈕處理接口，則註冊為按鈕處理器
    if (command instanceof ButtonHandler) {
      registerButtonHandler((ButtonHandler) command);
    }
    logger.debug("已註冊命令: {}", command.getName());
  }

  /**
   * 註冊按鈕處理器
   * @param handler 按鈕處理器
   */
  public void registerButtonHandler(ButtonHandler handler) {
    buttonHandlers.add(handler);
    logger.debug("已註冊按鈕處理器");
  }

  /**
   * 註冊所有命令到JDA
   * @param jda JDA實例
   */
  public void registerCommandsToJDA(JDA jda) {
    List<CommandData> commandDataList = new ArrayList<>();
    for (SlashCommand command : commands.values()) {
      commandDataList.add(command.getCommandData());
    }

    jda.updateCommands().addCommands(commandDataList).queue(
        success -> logger.info("已成功註冊所有斜線命令"),
        error -> logger.error("註冊斜線命令失敗", error)
    );
  }

  @Override
  public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
    String commandName = event.getName();
    SlashCommand command = commands.get(commandName);

    if (command != null) {
      try {
        command.execute(event);
      } catch (Exception e) {
        logger.error("執行命令 '{}' 時出錯", commandName, e);
        if (!event.isAcknowledged()) {
          event.reply("執行命令時發生錯誤！").setEphemeral(true).queue();
        }
      }
    }
  }

  @Override
  public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
    for (ButtonHandler handler : buttonHandlers) {
      try {
        if (handler.handleButtonInteraction(event)) {
          return; // 一旦有處理器處理了此事件，就不再繼續
        }
      } catch (Exception e) {
        logger.error("處理按鈕事件時出錯: {}", event.getComponentId(), e);
        if (!event.isAcknowledged()) {
          event.reply("處理操作時發生錯誤！").setEphemeral(true).queue();
        }
        return;
      }
    }

    // 如果沒有處理器處理此事件
    logger.warn("未找到處理按鈕 '{}' 的處理器", event.getComponentId());
  }
}