package per.iiimabbie.dcbot.util;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.Permission;
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
   * 檢查是否為 bot owner
   */
  public boolean isOwner(User user) {
    return botConfig.getOwnerId() != null &&
        botConfig.getOwnerId().equals(user.getId());
  }

  /**
   * 檢查owner權限，不是owner就拋異常
   */
  public void requireOwner(SlashCommandInteractionEvent event) {
    if (!isOwner(event.getUser())) {
      throw BotException.permissionDenied();
    }
  }

  /**
   * 檢查是否為伺服器管理員
   */
  public boolean isGuildAdmin(SlashCommandInteractionEvent event) {
    if (!event.isFromGuild()) return false;

    return event.getMember() != null &&
        event.getMember().hasPermission(Permission.ADMINISTRATOR);
  }

  /**
   * 檢查是否有指定權限
   */
  public boolean hasPermission(SlashCommandInteractionEvent event, Permission... permissions) {
    if (!event.isFromGuild()) return false;

    return event.getMember() != null &&
        event.getMember().hasPermission(permissions);
  }

  /**
   * 檢查是否有指定角色
   */
  public boolean hasRole(SlashCommandInteractionEvent event, String roleName) {
    if (!event.isFromGuild() || event.getMember() == null) return false;

    return event.getMember().getRoles().stream()
        .anyMatch(role -> role.getName().equalsIgnoreCase(roleName));
  }
}