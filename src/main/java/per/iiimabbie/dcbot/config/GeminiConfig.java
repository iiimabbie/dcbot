package per.iiimabbie.dcbot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "gemini")
public class GeminiConfig {

  private Api api;

  @Data
  public static class Api {

    private String url;
    private String key;
  }

}
