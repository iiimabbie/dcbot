package per.iiimabbie.dcbot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "bot")
public class BotConfig {

  private String name;
  private Status status;
  private String systemPrompt;
  private String ownerId;
  private String scheduledChannelId;

  @Data
  public static class Status {

    private String text;
  }
}