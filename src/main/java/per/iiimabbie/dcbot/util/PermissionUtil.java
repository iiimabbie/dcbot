package per.iiimabbie.dcbot.util;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;
import per.iiimabbie.dcbot.config.BotConfig;
import per.iiimabbie.dcbot.exception.BotException;

/**
 * 權限檢查工具
 */
@Component
@RequiredArgsConstructor
public class PermissionUtil {

  private final BotConfig botConfig;

  /**
   * 檢查是否為機器人主人
   */
  public boolean isOwner(User user) {
    return botConfig.getOwnerId() != null &&
        botConfig.getOwnerId().equals(user.getId());
  }

  /**
   * 檢查主人權限，不是主人就拋異常
   */
  public void requireOwner(SlashCommandInteractionEvent event) {
    if (!isOwner(event.getUser())) {
      throw BotException.permissionDenied();
    }
  }
}