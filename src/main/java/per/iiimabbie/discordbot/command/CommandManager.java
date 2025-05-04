package per.iiimabbie.discordbot.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
  private final Map<String, SlashCommand> commands = new HashMap<>(); // 全局命令
  private final Map<String, Map<String, SlashCommand>> guildCommands = new HashMap<>(); // Guild命令
  private final List<ButtonHandler> buttonHandlers = new ArrayList<>();

  /**
   * 註冊全局命令。
   * 全局命令可以在任何 Discord 伺服器中使用。
   *
   * @param command 要註冊的斜線命令實例
   * @throws IllegalArgumentException 如果命令名稱已存在
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
   *
   * @param handler 按鈕處理器
   */
  public void registerButtonHandler(ButtonHandler handler) {
    buttonHandlers.add(handler);
    logger.debug("已註冊按鈕處理器");
  }

  /**
   * 註冊所有命令到JDA
   *
   * @param jda JDA實例
   */
  public void registerCommandsToJDA(JDA jda) {
    List<CommandData> commandDataList = new ArrayList<>();
    for (SlashCommand command : commands.values()) {
      commandDataList.add(command.getCommandData());
    }

    jda.updateCommands().addCommands(commandDataList).queue(
        success -> logger.info("已成功註冊全局 {} 個命令", commandDataList.size()),
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

  // 註冊Guild專用命令
  public void registerGuildCommand(SlashCommand command, String guildId) {
    // 確保這個公會的Map已創建
    guildCommands.computeIfAbsent(guildId, k -> new HashMap<>());

    // 加入命令
    guildCommands.get(guildId).put(command.getName(), command);

    // 如果命令也實現了按鈕處理接口，註冊為按鈕處理器
    if (command instanceof ButtonHandler) {
      registerButtonHandler((ButtonHandler) command);
    }

    logger.debug("已為伺服器 {} 註冊命令: {}", guildId, command.getName());
  }

  // 註冊Guild命令到JDA
  public void registerGuildCommandsToJDA(JDA jda, String guildId) {
    if (!guildCommands.containsKey(guildId)) {
      logger.info("伺服器 {} 沒有註冊任何命令", guildId);
      return;
    }

    List<CommandData> commandDataList = new ArrayList<>();
    for (SlashCommand command : guildCommands.get(guildId).values()) {
      commandDataList.add(command.getCommandData());
    }

    Objects.requireNonNull(jda.getGuildById(guildId)).updateCommands().addCommands(commandDataList).queue(
        success -> logger.info("已成功在伺服器 {} 註冊 {} 個命令", guildId, commandDataList.size()),
        error -> logger.error("在伺服器 {} 註冊命令失敗", guildId, error)
    );
  }

  /**
   * 獲取所有已註冊的命令
   * @return 命令列表
   */
  public List<SlashCommand> getCommands() {
    return new ArrayList<>(commands.values());
  }
}