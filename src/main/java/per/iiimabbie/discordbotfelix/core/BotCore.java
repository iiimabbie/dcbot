package per.iiimabbie.discordbotfelix.core;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import per.iiimabbie.discordbotfelix.util.ConfigLoader;

/**
 * 機器人核心類，負責初始化和管理JDA實例
 */
public class BotCore {

  private static final Logger logger = LoggerFactory.getLogger(BotCore.class);
  private final JDA jda;

  /**
   * 初始化機器人核心
   *
   */
  public BotCore() {
    String token = ConfigLoader.get("discord.token");
    if (token == null || token.isEmpty()) {
      throw new IllegalStateException("Bot的token未設定");
    }

    String statusText = ConfigLoader.getOrDefault("bot.status.text", "太好了是藥劑師我們有救了😭");

    this.jda = JDABuilder.createDefault(token)
        .setActivity(Activity.customStatus(statusText))
        .enableIntents(
            GatewayIntent.GUILD_MESSAGES,
            GatewayIntent.GUILD_MEMBERS,
            GatewayIntent.MESSAGE_CONTENT
        )
        .setMemberCachePolicy(MemberCachePolicy.ALL)
        .setChunkingFilter(ChunkingFilter.ALL)
        .addEventListeners(new MessageListener())
        .build();

    logger.info("Bot核心初始化完成");
  }

  /**
   * 獲取JDA實例
   *
   * @return JDA實例
   */
  public JDA getJda() {
    return jda;
  }

  /**
   * 關閉機器人
   */
  public void shutdown() {
    logger.info("正在關閉機器人...");
    jda.shutdown();
  }
}