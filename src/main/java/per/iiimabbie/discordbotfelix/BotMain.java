package per.iiimabbie.discordbotfelix;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import per.iiimabbie.discordbotfelix.service.MessageListener;
import per.iiimabbie.discordbotfelix.util.ConfigLoader;

public class BotMain {
  private static final Logger logger = LoggerFactory.getLogger(BotMain.class);

  public static void main(String[] args) {

    // 獲取 Discord Bot Token
    String token = ConfigLoader.get("discord.token");
    if (token == null || token.isEmpty()) {
      logger.info("Discord token 未設定");
      return;
    }

    // 建立 JDA 實例
    try {
      JDABuilder.createDefault(token)
          .setActivity(Activity.customStatus("太好了是藥劑師我們有救了😭"))
          .enableIntents(
              GatewayIntent.GUILD_MESSAGES,
              GatewayIntent.GUILD_MEMBERS,
              GatewayIntent.MESSAGE_CONTENT
          )
          .setMemberCachePolicy(MemberCachePolicy.ALL)
          .setChunkingFilter(ChunkingFilter.ALL)
          .addEventListeners(new MessageListener())
          .build();

      logger.info("Bot 已啟動！");
    } catch (Exception e) {
      logger.error("Bot 啟動失敗: {}", e.getMessage());
    }
  }
}