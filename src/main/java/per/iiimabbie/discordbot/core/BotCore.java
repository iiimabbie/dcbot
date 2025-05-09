
package per.iiimabbie.discordbot.core;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import per.iiimabbie.discordbot.command.CommandManager;
import per.iiimabbie.discordbot.command.impl.ClearCommand;
import per.iiimabbie.discordbot.command.impl.HelpCommand;
import per.iiimabbie.discordbot.util.ConfigLoader;

/**
 * 機器人核心類，負責初始化和管理 Discord 機器人的所有核心組件。
 * 包括 JDA 實例、命令管理、事件監聽等。
 *
 * @author iiimabbie
 */
public class BotCore implements EventListener {

  private static final Logger logger = LoggerFactory.getLogger(BotCore.class);
  private final JDA jda;
  private final CommandManager commandManager;
//  private final String guildId = ConfigLoader.get("guild.id");

  /**
   * 初始化機器人核心
   */
  public BotCore() {
    String token = ConfigLoader.get("discord.token");
    String statusText = ConfigLoader.getOrDefault("bot.status.text", "太好了是藥劑師我們有救了😭");

    // 初始化命令管理器
    this.commandManager = new CommandManager();
    // 全局註冊
    commandManager.registerCommand(new ClearCommand());
    commandManager.registerCommand(new HelpCommand(commandManager));
    // 註冊更多命令...
    // GUILD註冊
//    commandManager.registerGuildCommand(new ClearCommand(), guildId);
//    commandManager.registerGuildCommand(new HelpCommand(commandManager), guildId);
    // 註冊更多命令...

    this.jda = JDABuilder.createDefault(token)
        .setActivity(Activity.customStatus(statusText))
        .enableIntents(
            GatewayIntent.GUILD_MESSAGES,
            GatewayIntent.GUILD_MEMBERS,
            GatewayIntent.MESSAGE_CONTENT
        )
        .setMemberCachePolicy(MemberCachePolicy.ALL)
        .setChunkingFilter(ChunkingFilter.ALL)
        .addEventListeners(
            new MessageListener(),
            commandManager,  // 直接使用命令管理器作為事件監聽器
            this
        )
        .build();

    logger.info("Bot核心初始化完成");
  }

  @Override
  public void onEvent(@NotNull GenericEvent event) {
    if (event instanceof ReadyEvent) {
      onReady();
    }
  }

  private void onReady() {
    logger.info("JDA 已準備就緒！");
    try {
      // 在 JDA 準備就緒後註冊斜線命令
      commandManager.registerCommandsToJDA(jda); // 全局
//      commandManager.registerGuildCommandsToJDA(jda, guildId); // Guild
    } catch (Exception e) {
      logger.error("註冊斜線命令失敗", e);
    }
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