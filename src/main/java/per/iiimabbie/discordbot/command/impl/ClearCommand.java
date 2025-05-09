package per.iiimabbie.discordbot.command.impl;

import java.util.Objects;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import per.iiimabbie.discordbot.command.SlashCommand;
import per.iiimabbie.discordbot.enums.ErrorEnums;
import per.iiimabbie.discordbot.util.EmbedUtils;

/**
 * Discord 清除訊息命令實現類。
 * <p>
 * 此命令允許具有訊息管理權限的使用者清除指定數量的聊天訊息。
 * 整個操作過程都是臨時性的（ephemeral），只有操作者能看見。
 * </p>
 *
 * @author iiimabbie
 */
public class ClearCommand implements SlashCommand {

  private static final Logger logger = LoggerFactory.getLogger(ClearCommand.class);
  private static final String COMMAND_NAME = "清空";
  private static final String COMMAND_DESC = "清空指定數量的聊天訊息";

  /**
   * 獲取命令的配置數據
   * <p>
   * 該方法定義了清除命令的結構，包括：
   * <ul>
   *   <li>命令名稱：清空</li>
   *   <li>命令描述：清空指定數量的聊天訊息</li>
   *   <li>必要參數：數量（整數類型）</li>
   *   <li>所需權限：MESSAGE_MANAGE（訊息管理權限）</li>
   * </ul>
   * </p>
   *
   * @return 命令數據對象，包含完整的命令配置
   */
  @Override
  public CommandData getCommandData() {
    return Commands.slash(COMMAND_NAME, COMMAND_DESC)
        .addOption(OptionType.INTEGER, "數量", "要清空的訊息數量", true)
        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_MANAGE));
  }

  /**
   * 執行清除命令的邏輯
   * <p>
   * 這個方法在用戶輸入 /清空 命令時被調用，執行流程如下：
   * <ol>
   *   <li>檢查用戶是否有MESSAGE_MANAGE權限</li>
   *   <li>獲取刪除的訊息數量</li>
   *   <li>直接執行清除操作</li>
   * </ol>
   * </p>
   *
   * @param event 斜線命令交互事件，包含命令參數和上下文
   */
  @Override
  public void execute(SlashCommandInteractionEvent event) {
    // 檢查是否有權限
    Member member = event.getMember();
    if (member == null || !member.hasPermission(Permission.MESSAGE_MANAGE)) {
      event.replyEmbeds(EmbedUtils.error(ErrorEnums.PERMISSION_DENIED)).setEphemeral(true).queue();
      return;
    }

    // 獲取要刪除的數量
    int amount = Objects.requireNonNull(event.getOption("數量")).getAsInt();

    // 直接執行清除操作
    executeClearMessages(event, amount);
  }

  /**
   * 執行訊息清除操作
   * <p>
   * 此方法實現實際的訊息清除邏輯：
   * <ol>
   *   <li>首先發送臨時回覆，表示命令已接收</li>
   *   <li>獲取指定數量的歷史訊息</li>
   *   <li>使用 Discord 的批次刪除功能清除訊息</li>
   *   <li>清除完成後向用戶發送完成通知</li>
   *   <li>記錄操作日誌，包含用戶名稱、頻道和清除數量</li>
   * </ol>
   * 如果過程中發生錯誤，會向用戶發送錯誤通知並記錄錯誤日誌。
   * </p>
   *
   * @param event 斜線命令事件，用於獲取頻道和發送響應
   * @param amount 要清除的訊息數量
   */
  private void executeClearMessages(SlashCommandInteractionEvent event, int amount) {
    // 首先延遲回應，告訴 Discord 你正在處理這個命令
    event.deferReply(true).queue();

    // 刪除訊息
    event.getChannel().getHistory().retrievePast(amount).queue(messages -> {
      // 批次刪除（Discord限制只能刪除兩週內的訊息）
      event.getChannel().purgeMessages(messages);

      // 使用 hook 發送後續訊息，告知結果
      event.getHook().sendMessageEmbeds(
          EmbedUtils.success("清空完成", String.format("已清除 %d 則訊息！", messages.size()))
      ).queue();

      logger.info("用戶 {} 在頻道 {} 清除了 {} 則訊息",
          event.getUser().getName(),
          event.getChannel().getName(),
          messages.size());
    }, error -> {
      event.getHook().sendMessageEmbeds(
          EmbedUtils.error(ErrorEnums.DEFAULT)
      ).queue();
      logger.error("清除訊息失敗", error);
    });
  }

  @Override
  public String getDescription() {
    return COMMAND_DESC;
  }

  @Override
  public String getName() {
    return COMMAND_NAME;
  }
}