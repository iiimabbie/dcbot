package per.iiimabbie.dcbot.service;

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

  private final Map<String, SlashCommand> commands = new HashMap<>();

  /**
   * 註冊指令
   */
  public void registerCommand(SlashCommand command) {
    commands.put(command.getName(), command);
    log.info("註冊指令: {}", command.getName());
  }

  /**
   * 取得指令
   */
  public SlashCommand getCommand(String name) {
    return commands.get(name);
  }

  /**
   * 取得所有註冊的指令 - 新增這個方法
   */
  public Map<String, SlashCommand> getAllCommands() {
    return new HashMap<>(commands); // 返回複本，避免外部修改
  }

  /**
   * 更新 Discord 上的指令
   */
  public void updateCommands(JDA jda) {
    List<CommandData> commandData = new ArrayList<>();

    for (SlashCommand command : commands.values()) {
      commandData.add(Commands.slash(command.getName(), command.getDescription()));
    }

    jda.updateCommands().addCommands(commandData).queue(
        success -> log.info("成功更新 {} 個 Slash Commands", commandData.size()),
        error -> log.error("更新 Slash Commands 失敗", error)
    );
  }
}