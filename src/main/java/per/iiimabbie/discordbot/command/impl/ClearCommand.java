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
 * Discord 清除訊息命令實現類。
 * <p>
 * 此命令允許具有訊息管理權限的使用者清除指定數量的聊天訊息。
 * 為防止誤操作，命令執行時會先顯示確認按鈕，用戶確認後才會執行實際的清除操作。
 * 整個操作過程都是臨時性的（ephemeral），只有操作者能看見。
 * </p>
 *
 * @author iiimabbie
 */
public class ClearCommand implements SlashCommand, ButtonHandler {

  private static final Logger logger = LoggerFactory.getLogger(ClearCommand.class);
  private static final String COMMAND_NAME = "清空";
  private static final String COMMAND_DESC = "清空指定數量的聊天訊息";
  private static final String BUTTON_CONFIRM = "confirm_clear";
  private static final String BUTTON_CANCEL = "cancel_clear";

  /**
   * 存儲待處理的清除請求
   * <p>
   * 鍵：用戶 ID
   * 值：要清除的訊息數量
   * </p>
   */
  private final Map<String, Integer> pendingClearRequests = new HashMap<>();

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
   *   <li>獲取並驗證要刪除的訊息數量（1-100之間）</li>
   *   <li>儲存清除請求到待處理映射中</li>
   *   <li>發送確認訊息，包含確認和取消按鈕</li>
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

  /**
   * 處理按鈕互動事件
   * <p>
   * 當用戶點擊確認或取消按鈕時調用此方法。根據按鈕 ID 執行不同的操作：
   * <ul>
   *   <li>確認按鈕：執行實際的訊息清除操作</li>
   *   <li>取消按鈕：取消清除請求，並刪除原始交互訊息</li>
   * </ul>
   * </p>
   *
   * @param event 按鈕互動事件，包含關於按鈕點擊的所有信息
   * @return 如果該處理器處理了此事件返回 true，否則返回 false
   */
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
   * @param event 按鈕互動事件，用於獲取頻道和發送響應
   * @param amount 要清除的訊息數量
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

  @Override
  public String getName() {
    return COMMAND_NAME;
  }
}