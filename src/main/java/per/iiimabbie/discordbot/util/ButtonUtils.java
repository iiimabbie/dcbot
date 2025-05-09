package per.iiimabbie.discordbot.util;

import net.dv8tion.jda.api.interactions.components.buttons.Button;
import java.util.ArrayList;
import java.util.List;

public class ButtonUtils {
  // 命名常數，避免硬編碼按鈕ID
  public static final String PREFIX_CONFIRM = "confirm_";
  public static final String PREFIX_CANCEL = "cancel_";
  public static final String PREFIX_NEXT = "next_";
  public static final String PREFIX_PREV = "prev_";

  /**
   * 創建確認按鈕組
   * @param actionName 操作名稱，用於生成按鈕ID
   * @return 確認和取消按鈕的列表
   */
  public static List<Button> createConfirmButtons(String actionName) {
    List<Button> buttons = new ArrayList<>();
    buttons.add(Button.danger(PREFIX_CONFIRM + actionName, "確認"));
    buttons.add(Button.secondary(PREFIX_CANCEL + actionName, "取消"));
    return buttons;
  }

  /**
   * 創建分頁按鈕組
   * @param actionName 操作名稱，用於生成按鈕ID
   * @param currentPage 當前頁碼
   * @param totalPages 總頁數
   * @return 上一頁和下一頁按鈕的列表
   */
  public static List<Button> createPaginationButtons(String actionName, int currentPage, int totalPages) {
    List<Button> buttons = new ArrayList<>();

    Button prevButton = Button.primary(PREFIX_PREV + actionName, "上一頁");
    Button nextButton = Button.primary(PREFIX_NEXT + actionName, "下一頁");

    // 如果在第一頁，禁用前一頁按鈕
    if (currentPage <= 1) {
      prevButton = prevButton.asDisabled();
    }

    // 如果在最後一頁，禁用下一頁按鈕
    if (currentPage >= totalPages) {
      nextButton = nextButton.asDisabled();
    }

    buttons.add(prevButton);
    buttons.add(nextButton);
    return buttons;
  }

  /**
   * 檢查按鈕ID是否屬於指定前綴
   * @param buttonId 按鈕ID
   * @param prefix 前綴
   * @return 是否匹配
   */
  public static boolean matchesPrefix(String buttonId, String prefix) {
    return buttonId != null && buttonId.startsWith(prefix);
  }

  /**
   * 從按鈕ID中提取操作名稱
   * @param buttonId 按鈕ID
   * @param prefix 前綴
   * @return 操作名稱
   */
  public static String extractActionName(String buttonId, String prefix) {
    if (matchesPrefix(buttonId, prefix)) {
      return buttonId.substring(prefix.length());
    }
    return null;
  }
}