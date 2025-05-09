package per.iiimabbie.discordbot.util;

import java.time.Instant;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import per.iiimabbie.discordbot.enums.ColorEnums;
import per.iiimabbie.discordbot.enums.ErrorEnums;

public class EmbedUtils {

  /**
   * 創建基本嵌入訊息
   */
  public static EmbedBuilder createEmbed(String title, String description, ColorEnums colorEnum) {
    return new EmbedBuilder()
        .setTitle(title)
        .setDescription(description)
        .setColor(colorEnum.getColor())
        .setTimestamp(Instant.now());
  }

  /**
   * 創建成功嵌入訊息
   */
  public static MessageEmbed success(String title, String description) {
    return createEmbed("💚｜" + title, description, ColorEnums.GREEN).build();
  }

  /**
   * 創建錯誤嵌入訊息
   */
  public static MessageEmbed error(String title, String description) {
    return createEmbed("💔｜" + title, description, ColorEnums.RED).build();
  }

  /**
   * 從錯誤枚舉創建錯誤嵌入訊息
   *
   * @param error 錯誤枚舉
   * @return 錯誤嵌入訊息
   */
  public static MessageEmbed error(ErrorEnums error) {
    return new EmbedBuilder()
        .setTitle("💔｜" + error.getTitle())
        .setDescription(error.getDescription())
        .setColor(ColorEnums.RED.getColor())
        .build();
  }

  /**
   * 從錯誤枚舉創建錯誤嵌入訊息，並格式化描述
   *
   * @param error 錯誤枚舉
   * @param args 描述格式化參數
   * @return 錯誤嵌入訊息
   */
  public static MessageEmbed error(ErrorEnums error, Object... args) {
    return new EmbedBuilder()
        .setTitle("💔｜" + error.getTitle())
        .setDescription(error.formatDescription(args))
        .setColor(ColorEnums.RED.getColor())
        .build();
  }


  /**
   * 創建資訊嵌入訊息
   */
  public static MessageEmbed info(String title, String description) {
    return createEmbed("🩵｜" + title, description, ColorEnums.BLUE).build();
  }

  /**
   * 創建幫助嵌入訊息
   */
  public static EmbedBuilder pageEmbed(String title, String description) {
    return createEmbed(title, description, ColorEnums.ORANGE);
  }

  /**
   * 添加頁碼頁尾
   */
  public static EmbedBuilder addPagination(EmbedBuilder builder, int currentPage, int totalPages) {
    if (totalPages > 1) {
      builder.setFooter(String.format("頁碼: %d/%d", currentPage, totalPages));
    }
    return builder;
  }
}