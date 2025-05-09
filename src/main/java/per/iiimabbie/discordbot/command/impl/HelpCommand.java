package per.iiimabbie.discordbot.command.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import per.iiimabbie.discordbot.command.ButtonHandler;
import per.iiimabbie.discordbot.command.CommandManager;
import per.iiimabbie.discordbot.command.SlashCommand;
import per.iiimabbie.discordbot.util.ButtonUtils;
import per.iiimabbie.discordbot.util.EmbedUtils;

/**
 * Discord 機器人幫助命令實現類。
 * <p>
 * 此命令提供一個交互式的幫助系統，以嵌入訊息形式顯示所有可用的機器人命令及其描述。
 * 為便於閱讀，幫助資訊採用分頁形式展示，每頁顯示固定數量的命令。
 * 使用者可以通過互動按鈕在不同頁面之間切換，查看全部命令。
 * </p>
 *
 * @author iiimabbie
 */
public class HelpCommand implements SlashCommand, ButtonHandler {

  private static final Logger logger = LoggerFactory.getLogger(HelpCommand.class);
  private static final String COMMAND_NAME = "幫助";
  private static final String COMMAND_DESC = "列出所有命令";
  private static final String ACTION_NAME = "help"; // 用於按鈕ID
  private static final int COMMANDS_PER_PAGE = 5;

  /**
   * 儲存用戶當前查看的幫助頁碼
   * <p>
   * 鍵：用戶 ID
   * 值：當前頁碼
   * </p>
   */
  private final Map<String, Integer> helpPages = new HashMap<>();
  private final CommandManager commandManager;

  /**
   * 構造一個新的幫助命令實例。
   *
   * @param commandManager 命令管理器，用於獲取已註冊的命令列表
   */
  public HelpCommand(CommandManager commandManager) {
    this.commandManager = commandManager;
  }

  /**
   * 獲取命令的配置數據
   * <p>
   * 該方法定義了幫助命令的結構，包括：
   * <ul>
   *   <li>命令名稱：幫助</li>
   *   <li>命令描述：列出所有命令</li>
   *   <li>權限設置：所有用戶均可使用</li>
   * </ul>
   * </p>
   *
   * @return 命令數據對象，包含完整的命令配置
   */
  @Override
  public CommandData getCommandData() {
    return Commands.slash(COMMAND_NAME, COMMAND_DESC)
        .setDefaultPermissions(DefaultMemberPermissions.ENABLED);
  }

  /**
   * 執行幫助命令的邏輯
   * <p>
   * 這個方法在用戶輸入 /幫助 命令時被調用，執行流程如下：
   * <ol>
   *   <li>創建第一頁的幫助嵌入訊息</li>
   *   <li>檢查命令總數是否需要分頁顯示</li>
   *   <li>如果不需要分頁，直接顯示單頁幫助資訊</li>
   *   <li>如果需要分頁，添加導航按鈕（上一頁/下一頁）</li>
   *   <li>儲存用戶的當前頁面狀態</li>
   * </ol>
   * 所有幫助訊息都設置為臨時訊息（ephemeral），只有請求的用戶可見。
   * </p>
   *
   * @param event 斜線命令交互事件，包含命令參數和上下文
   */
  @Override
  public void execute(SlashCommandInteractionEvent event) {
    logger.info("執行幫助命令");

    // 創建第一頁幫助資訊
    MessageEmbed helpEmbed = createHelpEmbed(1);

    // 獲取命令總數判斷是否需要分頁
    List<SlashCommand> commands = commandManager.getCommands();

    // 如果命令數量少於等於每頁顯示數量，不需要添加按鈕
    if (commands.size() <= COMMANDS_PER_PAGE) {
      event.replyEmbeds(helpEmbed)
          .setEphemeral(true)  // 設為臨時訊息
          .queue();
      return;
    }

    // 使用ButtonUtils創建分頁按鈕
    List<Button> paginationButtons = ButtonUtils.createPaginationButtons("help", 1, getTotalPages());

    // 回應斜線命令
    event.replyEmbeds(helpEmbed)
        .addActionRow(paginationButtons)
        .setEphemeral(true)  // 設為臨時訊息
        .queue(response -> {
          // 儲存用戶的頁面狀態
          helpPages.put(event.getUser().getId(), 1);
        });
  }

  /**
   * 處理按鈕互動事件
   * <p>
   * 當用戶點擊幫助頁面的導航按鈕時調用此方法。根據按鈕 ID 執行不同的操作：
   * <ul>
   *   <li>下一頁按鈕：顯示下一頁的命令列表</li>
   *   <li>上一頁按鈕：顯示上一頁的命令列表</li>
   * </ul>
   * 方法會確保頁碼在有效範圍內，並根據當前頁碼禁用或啟用相應的導航按鈕。
   * </p>
   *
   * @param event 按鈕互動事件，包含關於按鈕點擊的所有信息
   * @return 如果該處理器處理了此事件返回 true，否則返回 false
   */
  @Override
  public boolean handleButtonInteraction(ButtonInteractionEvent event) {
    String buttonId = event.getComponentId();

    // 使用ButtonUtils檢查按鈕ID是否匹配help前綴
    boolean isNext = ButtonUtils.matchesPrefix(buttonId, ButtonUtils.PREFIX_NEXT);
    boolean isPrev = ButtonUtils.matchesPrefix(buttonId, ButtonUtils.PREFIX_PREV);

    // 檢查是否是我們處理的按鈕類型
    if (!isNext && !isPrev) {
      return false;
    }
    // 提取操作名稱，確認是否是help操作
    String actionName = isNext ?
        ButtonUtils.extractActionName(buttonId, ButtonUtils.PREFIX_NEXT) :
        ButtonUtils.extractActionName(buttonId, ButtonUtils.PREFIX_PREV);

    if (!ACTION_NAME.equals(actionName)) {
      return false;
    }

    String userId = event.getUser().getId();
    int currentPage = helpPages.getOrDefault(userId, 1);

    if (isNext) {
      currentPage++;
    } else {
      currentPage--;
    }

    // 確保頁碼有效
    int totalPages = getTotalPages();
    currentPage = Math.max(1, Math.min(currentPage, totalPages));

    // 更新頁面狀態
    helpPages.put(userId, currentPage);

    // 創建新的嵌入和按鈕
    MessageEmbed helpEmbed = createHelpEmbed(currentPage);
    // 使用ButtonUtils創建更新後的分頁按鈕
    List<Button> updatedButtons = ButtonUtils.createPaginationButtons(ACTION_NAME, currentPage, totalPages);

    // 更新訊息
    event.editMessageEmbeds(helpEmbed)
        .setActionRow(updatedButtons)
        .queue();

    return true;
  }

  /**
   * 創建幫助嵌入訊息
   * <p>
   * 根據指定的頁碼創建一個包含命令列表的嵌入訊息。
   * 嵌入訊息包含標題、描述、顏色設定，以及當前頁的命令列表。
   * 每個命令以字段形式顯示，包含命令名稱和描述。
   * 如果總頁數大於 1，則在底部顯示當前頁碼和總頁數信息。
   * </p>
   *
   * @param page 要創建的頁碼，從 1 開始
   * @return 包含命令幫助信息的嵌入訊息對象
   */
  private MessageEmbed createHelpEmbed(int page) {
    EmbedBuilder embed = EmbedUtils.pageEmbed("機器人命令幫助", "以下是可用的命令列表，每個命令的用法和說明：");

    // 獲取命令列表
    List<SlashCommand> commands = commandManager.getCommands();
    int startIndex = (page - 1) * COMMANDS_PER_PAGE;
    int endIndex = Math.min(startIndex + COMMANDS_PER_PAGE, commands.size());

    for (int i = startIndex; i < endIndex; i++) {
      SlashCommand command = commands.get(i);
      embed.addField("/" + command.getName(), command.getDescription(), false);
    }

    // 添加分頁信息
    int totalPages = getTotalPages();
    EmbedUtils.addPagination(embed, page, totalPages);

    return embed.build();
  }

  /**
   * 計算幫助訊息的總頁數
   * <p>
   * 根據已註冊的命令總數和每頁顯示的命令數量，計算需要的總頁數。
   * 計算使用向上取整確保所有命令都能被顯示。
   * </p>
   *
   * @return 總頁數
   */
  private int getTotalPages() {
    int totalCommands = commandManager.getCommands().size();
    return (int) Math.ceil((double) totalCommands / COMMANDS_PER_PAGE);
  }

  @Override
  public String getName() {
    return COMMAND_NAME;
  }

  @Override
  public String getDescription() {
    return COMMAND_DESC;
  }
}