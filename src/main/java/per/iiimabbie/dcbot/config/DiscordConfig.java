package per.iiimabbie.dcbot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "discord")
public class DiscordConfig {
    private String token;
    private Guild guild;

    @Data
    public static class Guild {
        private String id;
    }
}