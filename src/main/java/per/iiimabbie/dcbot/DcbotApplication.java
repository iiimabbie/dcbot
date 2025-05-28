package per.iiimabbie.dcbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class DcbotApplication {

    public static void main(String[] args) {
        SpringApplication.run(DcbotApplication.class, args);
    }
}