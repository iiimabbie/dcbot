package per.iiimabbie.dcbot.config;

import java.time.Duration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate 配置
 * 統一管理 HTTP 請求的超時設定
 */
@Configuration
public class RestTemplateConfig {

  /**
   * 預設的 RestTemplate
   */
  @Bean("defaultRestTemplate")
  public RestTemplate defaultRestTemplate(RestTemplateBuilder builder) {
    return builder
        .requestFactory(() -> createRequestFactory(
            Duration.ofSeconds(10),  // 連線超時 10秒
            Duration.ofMinutes(1)   // 讀取超時 1分鐘
        ))
        .build();
  }

  /**
   * 長時間請求的 RestTemplate
   */
  @Bean("longTimeoutRestTemplate")
  public RestTemplate longTimeoutRestTemplate(RestTemplateBuilder builder) {
    return builder
        .requestFactory(() -> createRequestFactory(
            Duration.ofSeconds(15),   // 連線超時 15秒
            Duration.ofMinutes(2)     // 讀取超時 2分鐘
        ))
        .build();
  }

  /**
   * 創建 ClientHttpRequestFactory
   */
  private ClientHttpRequestFactory createRequestFactory(Duration connectTimeout, Duration readTimeout) {
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

    // SimpleClientHttpRequestFactory 使用 int 毫秒
    factory.setConnectTimeout((int) connectTimeout.toMillis());
    factory.setReadTimeout((int) readTimeout.toMillis());

    return factory;
  }
}