package per.iiimabbie.dcbot.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.stereotype.Service;
import per.iiimabbie.dcbot.config.BotConfig;
import per.iiimabbie.dcbot.config.DiscordConfig;
import per.iiimabbie.dcbot.listener.MessageListener;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscordService {

  private final DiscordConfig discordConfig;
  private final BotConfig botConfig;
  private final MessageListener messageListener;
  private final EmojiManager emojiManager;
  private JDA jda;

  @PostConstruct
  public void init() {
    try {
      log.info("正在初始化 Discord 機器人...");

      jda = JDABuilder.createDefault(discordConfig.getToken())
          .setActivity(Activity.customStatus(botConfig.getStatus().getText()))
          .setStatus(OnlineStatus.ONLINE)
          .enableIntents(
              GatewayIntent.GUILD_MESSAGES,
              GatewayIntent.MESSAGE_CONTENT,
              GatewayIntent.GUILD_MEMBERS
          )
          .addEventListeners(messageListener)
          .build();

      jda.awaitReady();
      
      // 初始化 emoji 管理器
      emojiManager.initialize(jda);
      log.info("Discord 機器人已啟動成功！");

    } catch (Exception e) {
      log.error("Discord 機器人初始化失敗", e);
    }
  }

  @PreDestroy
  public void shutdown() {
    if (jda != null) {
      jda.shutdown();
      log.info("Discord 機器人已關閉");
    }
  }

  // 更新機器人狀態
  public void updateStatus(String statusText) {
    if (jda != null) {
      jda.getPresence().setActivity(Activity.customStatus(statusText));
      log.info("已更新機器人狀態: {}", statusText);
    }
  }
}