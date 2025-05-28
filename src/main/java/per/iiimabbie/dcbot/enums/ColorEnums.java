package per.iiimabbie.dcbot.enums;

import java.awt.Color;
import lombok.Getter;

/**
 * 嵌入訊息顏色枚舉
 * <p>
 * 定義系統中使用的標準化顏色集合，採用莫蘭迪色系。
 * 設計用於在嵌入訊息中提供一致的視覺風格。
 * </p>
 *
 * @author iiimabbie
 */
@Getter
public enum ColorEnums {
  // 莫蘭迪色系
  RED(new Color(162, 103, 105), "莫蘭迪紅"),     // 錯誤、警告等
  ORANGE(new Color(216, 164, 127), "奶茶橘"),   // 幫助信息等
  YELLOW(new Color(238, 218, 163), "柔黃米"),   // 注意、提示等
  GREEN(new Color(168, 195, 161), "苔蘚綠"),    // 成功信息等
  BLUE(new Color(160, 191, 207), "霧藍"),      // 一般信息、對話等
  PURPLE(new Color(167, 154, 178), "煙紫");    // 特殊功能、高級選項等

  /**
   *  獲取顏色物件
   */
  private final Color color;
  /**
   *  獲取顏色描述
   */
  private final String description;

  /**
   * 創建一個帶有色彩值和描述的顏色枚舉
   *
   * @param color 顏色物件
   * @param description 顏色描述/名稱
   */
  ColorEnums(Color color, String description) {
    this.color = color;
    this.description = description;
  }

}