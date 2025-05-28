package per.iiimabbie.dcbot.command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/**
 * Slash Command 介面
 */
public interface SlashCommand {

  /**
   * 指令名稱
   */
  String getName();

  /**
   * 指令描述
   */
  String getDescription();

  /**
   * 執行指令
   */
  void execute(SlashCommandInteractionEvent event);
}