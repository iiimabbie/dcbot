
package per.iiimabbie.discordbotfelix.core;

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
import per.iiimabbie.discordbotfelix.command.CommandManager;
import per.iiimabbie.discordbotfelix.command.impl.ClearCommand;
import per.iiimabbie.discordbotfelix.util.ConfigLoader;

/**
 * 機器人核心類，負責初始化和管理JDA實例
 */
public class BotCore implements EventListener {

  private static final Logger logger = LoggerFactory.getLogger(BotCore.class);
  private final JDA jda;
  private final CommandManager commandManager;

  /**
   * 初始化機器人核心
   */
  public BotCore() {
    String token = ConfigLoader.get("discord.token");
    if (token == null || token.isEmpty()) {
      throw new IllegalStateException("Bot的token未設定");
    }

    String statusText = ConfigLoader.getOrDefault("bot.status.text", "太好了是藥劑師我們有救了😭");

    // 初始化命令管理器
    this.commandManager = new CommandManager();
    commandManager.registerCommand(new ClearCommand());
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
      commandManager.registerCommandsToJDA(jda);
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
   * 獲取命令管理器
   *
   * @return 命令管理器
   */
  public CommandManager getCommandManager() {
    return commandManager;
  }

  /**
   * 關閉機器人
   */
  public void shutdown() {
    logger.info("正在關閉機器人...");
    jda.shutdown();
  }
}