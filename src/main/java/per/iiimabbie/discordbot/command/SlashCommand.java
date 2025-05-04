package per.iiimabbie.discordbot.command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

/**
 * 斜線命令接口
 */
public interface SlashCommand {
  /**
   * 獲取命令配置
   * @return 命令數據
   */
  CommandData getCommandData();

  /**
   * 執行命令
   * @param event 斜線命令事件
   */
  void execute(SlashCommandInteractionEvent event);

  /**
   * 獲取命令名稱
   * @return 命令名稱
   */
  default String getName() {
    return getCommandData().getName();
  }

  /**
   * 獲取命令描述
   * @return 命令描述
   */
  default String getDescription() {
    return "未提供描述";  // default
  }

}