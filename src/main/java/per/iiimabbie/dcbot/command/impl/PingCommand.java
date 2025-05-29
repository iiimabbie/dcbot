package per.iiimabbie.dcbot.command.impl;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;
import per.iiimabbie.dcbot.command.SlashCommand;
import per.iiimabbie.dcbot.exception.BotException;

/**
 * Ping 指令 - 測試機器人回應延遲
 */
@Component
@RequiredArgsConstructor
public class PingCommand implements SlashCommand {

  @Override
  public String getName() {
    return "ping";
  }

  @Override
  public String getDescription() {
    return "測試機器人回應速度和延遲";
  }

  @Override
  public void execute(SlashCommandInteractionEvent event) {

    try {
      // 記錄開始時間
      long startTime = Instant.now().toEpochMilli();

      // 先回應一個臨時訊息
      event.reply("🏓 計算中...")
          .queue(interactionHook -> {
            // 計算回應時間
            long responseTime = Instant.now().toEpochMilli() - startTime;

            // 取得 WebSocket 延遲
            long gatewayPing = event.getJDA().getGatewayPing();

            // 建立詳細的延遲資訊
            String pingInfo = String.format(
                """
                    🏓 **Pong!**
                    
                    **WebSocket 延遲**: %d ms
                    **回應時間**: %d ms
                    **連線狀態**: %s""",
                gatewayPing,
                responseTime,
                getConnectionStatus(gatewayPing)
            );

            // 更新訊息
            interactionHook.editOriginal(pingInfo).queue();
          });

    } catch (Exception e) {
      throw new BotException(BotException.ErrorType.DISCORD_API_ERROR,
          "無法測試延遲", e);
    }
  }

  /**
   * 根據延遲判斷連線狀態
   */
  private String getConnectionStatus(long ping) {
    if (ping < 100) {
      return "🟢 優秀";
    } else if (ping < 200) {
      return "🟡 良好";
    } else if (ping < 500) {
      return "🟠 一般";
    } else {
      return "🔴 較慢";
    }
  }
}