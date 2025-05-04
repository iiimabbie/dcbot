package per.iiimabbie.discordbot.command.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import per.iiimabbie.discordbot.command.ButtonHandler;
import per.iiimabbie.discordbot.command.SlashCommand;

/**
 * 清除訊息命令
 */
public class ClearCommand implements SlashCommand, ButtonHandler {

  private static final Logger logger = LoggerFactory.getLogger(ClearCommand.class);
  private static final String COMMAND_NAME = "清空";
  private static final String COMMAND_DESC = "清空指定數量的聊天訊息";
  private static final String BUTTON_CONFIRM = "confirm_clear";
  private static final String BUTTON_CANCEL = "cancel_clear";

  private final Map<String, Integer> pendingClearRequests = new HashMap<>();

  @Override
  public CommandData getCommandData() {
    return Commands.slash(COMMAND_NAME, COMMAND_DESC)
        .addOption(OptionType.INTEGER, "數量", "要清空的訊息數量", true)
        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_MANAGE));
  }

  @Override
  public void execute(SlashCommandInteractionEvent event) {
    // 檢查是否有權限
    Member member = event.getMember();
    if (member == null || !member.hasPermission(Permission.MESSAGE_MANAGE)) {
      event.reply("你沒有權限執行此命令！").setEphemeral(true).queue();
      return;
    }

    // 獲取要刪除的數量
    int amount = Objects.requireNonNull(event.getOption("數量")).getAsInt();

    // 檢查數量是否有效
    if (amount < 1 || amount > 100) {
      event.reply("請指定1到100之間的數量！").setEphemeral(true).queue();
      return;
    }

    // 儲存這個待處理的請求
    String userId = event.getUser().getId();
    pendingClearRequests.put(userId, amount);

    // 發送確認訊息
    event.reply("確定要清除 " + amount + " 則訊息嗎？").setEphemeral(true)
        .addActionRow(
            Button.danger(BUTTON_CONFIRM, "確認清除"),
            Button.secondary(BUTTON_CANCEL, "取消")
        ).queue();
  }

  @Override
  public boolean handleButtonInteraction(ButtonInteractionEvent event) {
    String buttonId = event.getComponentId();

    // 檢查是否是我們處理的按鈕
    if (!buttonId.equals(BUTTON_CONFIRM) && !buttonId.equals(BUTTON_CANCEL)) {
      return false;
    }

    String userId = event.getUser().getId();

    // 如果是確認按鈕
    if (buttonId.equals(BUTTON_CONFIRM)) {
      // 檢查是否有待處理的清除請求
      if (pendingClearRequests.containsKey(userId)) {
        int amount = pendingClearRequests.get(userId);

        // 執行清除操作前刪除原始訊息
        event.getMessage().delete().queue();

        // 執行清除操作
        executeClearMessages(event, amount);
        pendingClearRequests.remove(userId); // 移除請求
      } else {
        event.reply("找不到您的清除請求。請重新使用 /清空 命令。").setEphemeral(true).queue();
      }
    } else {
      // 取消清除請求
      pendingClearRequests.remove(userId);
      // 先回應交互
      event.reply("已取消清除訊息操作。").setEphemeral(true).queue(
          success -> {
            // 然後再刪除消息
            event.getMessage().delete().queue(
                null,
                error -> logger.error("刪除原始訊息時發生錯誤", error)
            );
          }
      );
    }

    return true;
  }

  /**
   * 執行訊息清除操作
   */
  private void executeClearMessages(ButtonInteractionEvent event, int amount) {
    // 先回覆，表示命令已接收
    event.deferReply(true).queue();

    // 刪除訊息
    event.getChannel().getHistory().retrievePast(amount).queue(messages -> {
      // 批次刪除（Discord限制只能刪除兩週內的訊息）
      event.getChannel().purgeMessages(messages);

      // 完成後通知
      event.getHook().sendMessage(String.format("已清除 %d 則訊息！", messages.size())).queue();
      logger.info("用戶 {} 在頻道 {} 清除了 {} 則訊息",
          event.getUser().getName(),
          event.getChannel().getName(),
          messages.size());
    }, error -> {
      event.getHook().sendMessage("清除訊息時發生錯誤！").queue();
      logger.error("清除訊息失敗", error);
    });
  }

  @Override
  public String getDescription() {
    return COMMAND_DESC;
  }
}