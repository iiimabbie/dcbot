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
import per.iiimabbie.dcbot.command.impl.CommandsCommand;
import per.iiimabbie.dcbot.command.impl.HelpCommand;
import per.iiimabbie.dcbot.command.impl.PingCommand;
import per.iiimabbie.dcbot.command.impl.StatusCommand;
import per.iiimabbie.dcbot.config.BotConfig;
import per.iiimabbie.dcbot.config.DiscordConfig;
import per.iiimabbie.dcbot.listener.ButtonInteractionListener;
import per.iiimabbie.dcbot.listener.MessageListener;
import per.iiimabbie.dcbot.listener.SlashCommandListener;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscordService {

  private final DiscordConfig discordConfig;
  private final BotConfig botConfig;
  private final MessageListener messageListener;
  private final SlashCommandListener slashCommandListener;
  private final ButtonInteractionListener buttonInteractionListener;
  private final EmojiManager emojiManager;
  private final CommandManager commandManager;
  private final HelpCommand helpCommand;
  private final CommandsCommand commandsCommand;
  private final PingCommand pingCommand;
  private final StatusCommand statusCommand;
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
          .addEventListeners(messageListener, slashCommandListener, buttonInteractionListener)
          .build();

      jda.awaitReady();
      
      // 初始化 emoji 管理器
      emojiManager.initialize(jda);

      // 註冊所有指令
      registerCommands();

      log.info("Discord 機器人已啟動成功！");

    } catch (Exception e) {
      log.error("Discord 機器人初始化失敗", e);
    }
  }

  /**
   * 註冊所有指令
   */
  private void registerCommands() {
    // 註冊public指令
    commandManager.registerGlobalCommand(helpCommand);
    commandManager.registerGlobalCommand(commandsCommand);
    commandManager.registerGlobalCommand(pingCommand);
    commandManager.registerGlobalCommand(statusCommand);
    // 註冊private專用指令

    // 更新 Discord 上的指令
    commandManager.updateCommands(jda, discordConfig.getGuild().getId());
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