package per.iiimabbie.dcbot.command.impl;

import java.time.Instant;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;
import per.iiimabbie.dcbot.command.SlashCommand;
import per.iiimabbie.dcbot.enums.ColorEnums;
import per.iiimabbie.dcbot.exception.BotException;
import per.iiimabbie.dcbot.service.CommandManager;

/**
 * Commands 指令 - 顯示所有可用指令
 */
@Component
@RequiredArgsConstructor
public class CommandsCommand implements SlashCommand {

  private final CommandManager commandManager;

  @Override
  public String getName() {
    return "commands";
  }

  @Override
  public String getDescription() {
    return "顯示所有可用的指令列表";
  }

  @Override
  public void execute(SlashCommandInteractionEvent event) {
    try {
      MessageEmbed commandsEmbed = createCommandsEmbed();

      event.replyEmbeds(commandsEmbed)
          .setEphemeral(true)
          .queue();
    } catch (Exception e) {
      throw new BotException(BotException.ErrorType.DISCORD_API_ERROR,
          "無法建立指令列表", e);
    }
  }

  public MessageEmbed createCommandsEmbed() {
    EmbedBuilder builder = new EmbedBuilder()
        .setTitle("📋 指令列表")
        .setDescription("以下是所有可用的指令：")
        .setColor(ColorEnums.PURPLE.getColor())
        .setTimestamp(Instant.now());

    // 動態從 CommandManager 取得所有指令
    Map<String, SlashCommand> allCommands = commandManager.getAllCommands();

    if (allCommands.isEmpty()) {
      builder.addField("😅 沒有指令", "目前沒有可用的指令", false);
    } else {
      StringBuilder commandList = new StringBuilder();

      for (SlashCommand command : allCommands.values()) {
        commandList.append("`/")
            .append(command.getName())
            .append("` - ")
            .append(command.getDescription())
            .append("\n");
      }

      builder.addField("⚡ 可用指令 (" + allCommands.size() + ")",
          commandList.toString(), false);
    }

    builder.addField("💬 聊天功能",
        "直接 @ 機器人即可開始聊天\n支援討論串和私訊", false);

    builder.setFooter("提示：使用 / 開頭會有自動補完功能", null);

    return builder.build();
  }
}