package per.iiimabbie.discordbotfelix.core;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import per.iiimabbie.discordbotfelix.command.CommandManager;
import per.iiimabbie.discordbotfelix.command.impl.ClearCommand;

/**
 * 斜線命令監聽器
 */
public class SlashCommandListener extends ListenerAdapter {

  private static final Logger logger = LoggerFactory.getLogger(SlashCommandListener.class);
  private final CommandManager commandManager;

  public SlashCommandListener() {
    this.commandManager = new CommandManager();

    // 註冊命令
    commandManager.registerCommand(new ClearCommand());

    // 在這裡註冊更多命令
    // commandManager.registerCommand(new AnotherCommand());
  }

  /**
   * 註冊所有斜線命令
   */
  public static void registerCommands(JDA jda) {
    // 創建一個臨時的命令管理器來註冊命令
    CommandManager manager = new CommandManager();
    manager.registerCommand(new ClearCommand());

    // 註冊命令到JDA
    manager.registerCommandsToJDA(jda);
  }

  @Override
  public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
    // 委派給命令管理器處理
    commandManager.onSlashCommandInteraction(event);
  }

  @Override
  public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
    // 委派給命令管理器處理
    commandManager.onButtonInteraction(event);
  }
}