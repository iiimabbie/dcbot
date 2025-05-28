package per.iiimabbie.dcbot.command.impl.owner;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.springframework.stereotype.Component;
import per.iiimabbie.dcbot.command.SlashCommand;
import per.iiimabbie.dcbot.config.BotConfig;
import per.iiimabbie.dcbot.enums.ColorEnums;
import per.iiimabbie.dcbot.exception.BotException;
import per.iiimabbie.dcbot.util.PermissionUtil;

/**
 * Status 指令 - 顯示機器人狀態
 */
@Component
@RequiredArgsConstructor
public class StatusCommand implements SlashCommand {

  private final BotConfig botConfig;
  private final PermissionUtil permissionUtil;

  // 記錄啟動時間
  private static final long START_TIME = System.currentTimeMillis();

  @Override
  public String getName() {
    return "status";
  }

  @Override
  public String getDescription() {
    return "查看機器人運行狀態和系統資訊";
  }

  @Override
  public void execute(SlashCommandInteractionEvent event) {

    // 權限檢查
    permissionUtil.requireOwner(event);

    try {
      MessageEmbed statusEmbed = createStatusEmbed(event);

      // 添加重新整理按鈕
      Button refreshButton = Button.secondary("refresh_status", "🔄 重新整理");
      Button pingButton = Button.primary("quick_ping", "🏓 測試延遲");

      event.replyEmbeds(statusEmbed)
          .addActionRow(refreshButton, pingButton)
          .setEphemeral(true)
          .queue();

    } catch (Exception e) {
      throw new BotException(BotException.ErrorType.DISCORD_API_ERROR,
          "無法取得機器人狀態", e);
    }
  }

  /**
   * 建立狀態資訊 Embed
   */
  public MessageEmbed createStatusEmbed(SlashCommandInteractionEvent event) {
    // 計算運行時間
    long uptime = System.currentTimeMillis() - START_TIME;
    String uptimeStr = formatUptime(uptime);

    // 記憶體資訊
    Runtime runtime = Runtime.getRuntime();
    long totalMemory = runtime.totalMemory() / (1024 * 1024); // MB
    long freeMemory = runtime.freeMemory() / (1024 * 1024);   // MB
    long usedMemory = totalMemory - freeMemory;
    long maxMemory = runtime.maxMemory() / (1024 * 1024);     // MB

    // 系統資訊
    String javaVersion = System.getProperty("java.version");
    String osName = System.getProperty("os.name");

    // Discord 連線資訊
    long gatewayPing = event.getJDA().getGatewayPing();
    int guildCount = event.getJDA().getGuilds().size();

    return new EmbedBuilder()
        .setTitle("🤖 " + botConfig.getName() + " 狀態報告")
        .setDescription("機器人目前運行正常 ✅")
        .setColor(ColorEnums.GREEN.getColor())
        .setThumbnail(event.getJDA().getSelfUser().getAvatarUrl())

        // 基本資訊
        .addField("⏰ 運行時間", uptimeStr, true)
        .addField("🏓 延遲", gatewayPing + " ms", true)
        .addField("🏠 伺服器數量", String.valueOf(guildCount), true)

        // 記憶體資訊
        .addField("💾 記憶體使用",
            String.format("已使用: %d MB\n總共: %d MB\n最大: %d MB",
                usedMemory, totalMemory, maxMemory), true)

        // 系統資訊
        .addField("⚙️ 系統資訊",
            String.format("Java: %s\nOS: %s", javaVersion, osName), true)

        // 當前狀態
        .addField("📊 當前狀態",
            String.format("狀態: %s\n活動: %s",
                event.getJDA().getStatus().name(),
                botConfig.getStatus().getText()), true)

        .setFooter("最後更新", null)
        .setTimestamp(Instant.now())
        .build();
  }

  /**
   * 格式化運行時間
   */
  private String formatUptime(long uptimeMs) {
    long seconds = uptimeMs / 1000;
    long minutes = seconds / 60;
    long hours = minutes / 60;
    long days = hours / 24;

    if (days > 0) {
      return String.format("%d 天 %d 小時 %d 分鐘",
          days, hours % 24, minutes % 60);
    } else if (hours > 0) {
      return String.format("%d 小時 %d 分鐘", hours, minutes % 60);
    } else if (minutes > 0) {
      return String.format("%d 分鐘 %d 秒", minutes, seconds % 60);
    } else {
      return seconds + " 秒";
    }
  }
}