package per.iiimabbie.discordbot.command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

/**
 * 斜線命令接口，定義 Discord 斜線命令的基本結構和行為。
 * 所有的斜線命令實現類都應該實現此接口。
 *
 * @author iiimabbie
 */
public interface SlashCommand {
  /**
   * 獲取命令配置數據，包括命令名稱、描述、參數等。
   *
   * @return 命令數據對象，包含完整的命令配置
   */
  CommandData getCommandData();

  /**
   * 執行命令的邏輯。
   * 當用戶在 Discord 中觸發斜線命令時，該方法會被調用。
   *
   * @param event 斜線命令交互事件，包含命令參數和上下文
   */
  void execute(SlashCommandInteractionEvent event);

  /**
   * 獲取命令名稱。
   * 默認實現從 CommandData 中獲取名稱。
   *
   * @return 命令名稱字符串
   */
  default String getName() {
    return getCommandData().getName();
  }

  /**
   * 獲取命令描述。
   * 此方法可被覆寫以提供更詳細的描述。
   *
   * @return 命令描述字符串
   */
  default String getDescription() {
    return "未提供描述";  // default
  }

}