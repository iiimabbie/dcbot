package per.iiimabbie.dcbot.service;

import java.time.Instant;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.springframework.stereotype.Service;
import per.iiimabbie.dcbot.enums.ColorEnums;

/**
 * Discord 訊息建構服務
 */
@Service
public class MessageBuilderService {

  /**
   * 建立基本 Embed 訊息
   */
  public MessageEmbed createBasicEmbed(String title, String description, ColorEnums color) {
    return new EmbedBuilder()
        .setTitle(title)
        .setDescription(description)
        .setColor(color.getColor())
        .setTimestamp(Instant.now())
        .build();
  }

  /**
   * 建立成功訊息
   */
  public MessageEmbed createSuccessEmbed(String message) {
    return createBasicEmbed("✅ 成功", message, ColorEnums.GREEN);
  }

  /**
   * 建立錯誤訊息
   */
  public MessageEmbed createErrorEmbed(String message) {
    return createBasicEmbed("❌ 錯誤", message, ColorEnums.RED);
  }

  /**
   * 建立警告訊息
   */
  public MessageEmbed createWarningEmbed(String message) {
    return createBasicEmbed("⚠️ 警告", message, ColorEnums.YELLOW);
  }

  /**
   * 建立資訊訊息
   */
  public MessageEmbed createInfoEmbed(String title, String message) {
    return createBasicEmbed("ℹ️ " + title, message, ColorEnums.BLUE);
  }

  /**
   * 建立幫助訊息
   */
  public MessageEmbed createHelpEmbed(String commandName, String description, String usage) {
    return new EmbedBuilder()
        .setTitle("📚 指令說明 - " + commandName)
        .setDescription(description)
        .addField("使用方法", usage, false)
        .setColor(ColorEnums.PURPLE.getColor())
        .setTimestamp(Instant.now())
        .build();
  }

  /**
   * 建立帶按鈕的訊息
   */
  public Button createPrimaryButton(String id, String label) {
    return Button.primary(id, label);
  }

  public Button createSecondaryButton(String id, String label) {
    return Button.secondary(id, label);
  }

  public Button createDangerButton(String id, String label) {
    return Button.danger(id, label);
  }
}