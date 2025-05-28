package per.iiimabbie.dcbot.service;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.stereotype.Service;
import per.iiimabbie.dcbot.command.SlashCommand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Slash Command 管理服務
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommandManager {

  private final Map<String, SlashCommand> globalCommands = new HashMap<>();
  private final Map<String, SlashCommand> guildCommands = new HashMap<>(); // owner專用指令

  /**
   * 註冊global指令
   */
  public void registerGlobalCommand(SlashCommand command) {
    globalCommands.put(command.getName(), command);
    log.info("註冊global指令: {}", command.getName());
  }

  /**
   * 註冊guild專用指令
   */
  public void registerGuildCommand(SlashCommand command) {
    guildCommands.put(command.getName(), command);
    log.info("註冊private指令: {}", command.getName());
  }

  /**
   * 找指令
   */
  public SlashCommand getCommand(String name) {
    // 先從全域指令找，再從私有指令找
    SlashCommand command = globalCommands.get(name);
    if (command == null) {
      command = guildCommands.get(name);
    }
    return command;
  }

  /**
   * 取得所有註冊的全域指令
   */
  public Map<String, SlashCommand> getAllCommands() {
    return new HashMap<>(globalCommands);
  }

  /**
   * 更新 Discord 上的指令
   */
  public void updateCommands(JDA jda, String guildId) {
    // public slash
    List<CommandData> globalCommands = new ArrayList<>();
    for (SlashCommand command : this.globalCommands.values()) {
      globalCommands.add(Commands.slash(command.getName(), command.getDescription()));
    }

    jda.updateCommands().addCommands(globalCommands).queue(
        success -> log.info("成功更新 {} 個 Global Slash Commands", globalCommands.size()),
        error -> log.error("更新 Global Slash Commands 失敗", error)
    );

    // private slash
    if (guildId != null && !guildCommands.isEmpty()) {
      List<CommandData> guildCommands = new ArrayList<>();
      for (SlashCommand command : this.guildCommands.values()) {
        guildCommands.add(Commands.slash(command.getName(), command.getDescription()));
      }

      Objects.requireNonNull(jda.getGuildById(guildId)).updateCommands().addCommands(guildCommands).queue(
          success -> log.info("成功更新 {} 個 Guild Slash Commands", guildCommands.size()),
          error -> log.error("更新 Guild Slash Commands 失敗", error)
      );
    }
  }
}