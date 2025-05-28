package per.iiimabbie.dcbot.command.impl;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.springframework.stereotype.Component;
import per.iiimabbie.dcbot.command.SlashCommand;
import per.iiimabbie.dcbot.config.BotConfig;
import per.iiimabbie.dcbot.enums.ColorEnums;
import per.iiimabbie.dcbot.service.MessageBuilderService;

import java.time.Instant;

/**
 * Help 指令 - 顯示機器人使用說明
 */
@Component
@RequiredArgsConstructor
public class HelpCommand implements SlashCommand {

  private final BotConfig botConfig;
  private final MessageBuilderService messageBuilder;

  @Override
  public String getName() {
    return "help";
  }

  @Override
  public String getDescription() {
    return "顯示機器人使用說明和指令列表";
  }

  @Override
  public void execute(SlashCommandInteractionEvent event) {
    MessageEmbed helpEmbed = createHelpEmbed();

    // 創建按鈕
    Button commandsButton = Button.primary("show_commands", "📋 查看所有指令");
    Button supportButton = Button.link("https://github.com/iiimabbie/dcbot", "🆘 我的家");

    event.replyEmbeds(helpEmbed)
        .addActionRow(commandsButton, supportButton)
        .queue();
  }

  /**
   * 創建幫助說明 Embed
   */
  private MessageEmbed createHelpEmbed() {
    return new EmbedBuilder()
        .setTitle("🤖 " + botConfig.getName() + " 使用說明")
        .setDescription("嗨～我是 " + botConfig.getName() + "，一隻可愛的 Discord 機器人！")
        .setColor(ColorEnums.BLUE.getColor())
        .setThumbnail("https://cdn.discordapp.com/embed/avatars/0.png") // TODO 有空找一下地方放機器人頭像

        // 基本功能
        .addField("\n💬 聊天功能",
            "• 直接 `@" + botConfig.getName() + "` 就可以跟我聊天\n" +
                "• 在討論串中我會自動參與對話\n" +
                "• 支援私訊聊天",
            false)

        // 指令功能
        .addField("\n⚡ 指令功能",
            "• 使用 `/help` 顯示這個說明\n" +
                "• 使用 `/commands` 查看所有可用指令\n" +
                "• 更多功能持續開發中...",
            false)

        // 特色功能
        .addField("\n✨ 特色功能",
            "• 智能對話 (由 Gemini AI 驅動)\n" +
                "• 能傳送約50則上文\n" +
                "• 可愛的表情符號反應(但是還沒做)\n" + // TODO 這還沒搞 想玩aiagent
                "• 支援討論串和群組聊天",
            false)

        // 使用提示
        .addField("\n💡 使用提示",
            "• 如果我沒回應，可能是在思考中，請稍等\n" +
                "• 遇到問題可以重新 @ 我\n" +
                "• 想要私密聊天可以發私訊給我",
            false)

        .setFooter("Created with ❤️ | " + botConfig.getName(), null)
        .setTimestamp(Instant.now())
        .build();
  }
}