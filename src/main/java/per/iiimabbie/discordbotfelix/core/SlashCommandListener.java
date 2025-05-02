package per.iiimabbie.discordbotfelix.core;

import java.util.List;
import java.util.concurrent.TimeUnit;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SlashCommandListener extends ListenerAdapter {
  private static final Logger logger = LoggerFactory.getLogger(SlashCommandListener.class);
  private static final int MAX_MESSAGES_PER_BATCH = 100; // Discord API 限制

  @Override
  public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
    if (event.getName().equals("clear")) {
      handleClearCommand(event);
    }
  }

  private void handleClearCommand(SlashCommandInteractionEvent event) {
    // 檢查權限
    Member member = event.getMember();
    if (member == null || !member.hasPermission(Permission.MESSAGE_MANAGE)) {
      event.reply("你沒有權限使用這個命令。").setEphemeral(true).queue();
      return;
    }

    // 收到指令先回應，避免 Discord 超時
    event.deferReply(true).queue();

    // 獲取當前頻道
    if (!(event.getChannel() instanceof TextChannel textChannel)) {
      event.getHook().sendMessage("只能在文字頻道使用此命令。").setEphemeral(true).queue();
      return;
    }

    try {
      // 清除訊息
      int deletedCount = clearMessages(textChannel);

      // 發送結果
      event.getHook().sendMessage("已清除 " + deletedCount + " 條訊息。").setEphemeral(true).queue();

      // 發送一條自動刪除的通知到頻道
      textChannel.sendMessage("Felix 已清除了此頻道的歷史訊息。").queue(
          message -> message.delete().queueAfter(5, TimeUnit.SECONDS)
      );

      logger.info("用戶 {} 在頻道 {} 清除了 {} 條訊息",
          member.getUser().getName(), textChannel.getName(), deletedCount);
    } catch (Exception e) {
      logger.error("清除訊息時出錯", e);
      event.getHook().sendMessage("清除訊息時發生錯誤：" + e.getMessage()).setEphemeral(true).queue();
    }
  }

  private int clearMessages(TextChannel channel) {
    int totalDeleted = 0;
    boolean hasMoreMessages = true;

    while (hasMoreMessages) {
      MessageHistory history = channel.getHistory();
      List<Message> messages = history.retrievePast(MAX_MESSAGES_PER_BATCH).complete();

      if (messages.isEmpty()) {
        hasMoreMessages = false;
        continue;
      }

      List<Message> messagesToDelete = messages.stream()
          .filter(msg -> !msg.isPinned()) // 不刪除置頂訊息
          .toList();

      if (messagesToDelete.isEmpty()) {
        hasMoreMessages = false;
        continue;
      }

      if (messagesToDelete.size() == 1) {
        messagesToDelete.getFirst().delete().complete();
        totalDeleted += 1;
      } else {
        channel.deleteMessages(messagesToDelete).complete();
        totalDeleted += messagesToDelete.size();
      }

      // 避免 API 限制，稍微暫停一下
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      }
    }

    return totalDeleted;
  }

  // 註冊斜線指令
  public static SlashCommandData getClearCommandData() {
    return Commands.slash("clear", "清除頻道中的所有訊息")
        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_MANAGE))
        .setGuildOnly(true);
  }
}