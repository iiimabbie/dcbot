package per.iiimabbie.discordbotfelix.enums;

public enum Command {
  AI("ai"),
  RESET("reset"),
  RELOAD("reload");

  private final String name;

  Command(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  // 根據命令名稱查找枚舉
  public static Command fromString(String text) {
    for (Command cmd : Command.values()) {
      if (cmd.name.equalsIgnoreCase(text)) {
        return cmd;
      }
    }
    return null;
  }

}
