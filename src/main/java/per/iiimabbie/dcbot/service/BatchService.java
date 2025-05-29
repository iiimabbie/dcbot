package per.iiimabbie.dcbot.service;

import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import per.iiimabbie.dcbot.config.BotConfig;

@Service
@Slf4j
public class BatchService {

  private JDA jda;

  private final BotConfig botConfig;
  private final DiscordService discordService;
  private final Random random = new Random();

  public BatchService(BotConfig botConfig, DiscordService discordService) {
    this.botConfig = botConfig;
    this.discordService = discordService;
  }

  /**
   * 每天12:00執行此方法
   */
  @Scheduled(cron = "0 0 12 * * ?")
  public void sendDailyMessage() {
    try {
      // 從 DiscordService 獲取最新的 JDA 實例
      if (jda == null) {
        jda = discordService.getJda();
        if (jda == null) {
          log.error("JDA 實例尚未初始化，無法發送每日訊息");
          return;
        }
      }

      TextChannel channel = jda.getTextChannelById(botConfig.getScheduledChannelId());
      if (channel != null) {
        int result = random.nextInt(100) + 1;

        String dailyMessage = String.format("""
                ### 養肝計劃 🥵
                擲個百面骰，比1d100：
                %d[%d] = %d 還小的都去睡覺！
                """,
            result, result, result
        );

        channel.sendMessage(dailyMessage).queue();
        log.info("每日養肝計劃消息已發送至頻道: {}", channel.getName());
      } else {
        log.error("找不到設定的頻道 ID: {}", botConfig.getScheduledChannelId());
      }
    } catch (Exception e) {
      log.error("發送每日消息時發生錯誤", e);
    }
  }
}