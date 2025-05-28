package per.iiimabbie.dcbot.enums;

import lombok.Getter;

public class BotEmojis {

  @Getter
  public enum Tool {
    LOADING("loading"),
    SUCCESS("success"),
    ERROR("error");

    private final String name;

    Tool(String name) {
      this.name = name;
    }
  }

  @Getter
  public enum Emotion {
    ANGRY("angry"),
    SPEECHLESS("speechless"),
    CRY("cry"),
    SAD("sad"),
    UNHAPPY("unhappy"),
    HAPPY("happy");

    private final String name;

    Emotion(String name) {
      this.name = name;
    }
  }

  @Getter
  public enum Dice {
    ONE("dice_1"),
    TWO("dice_2"),
    THREE("dice_3"),
    FOUR("dice_4"),
    FIVE("dice_5"),
    SIX("dice_6");

    private final String name;

    Dice(String name) {
      this.name = name;
    }
  }
}
