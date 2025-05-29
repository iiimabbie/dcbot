package per.iiimabbie.dcbot.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import java.awt.Color;
import lombok.Setter;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import per.iiimabbie.dcbot.enums.ColorEnums;

@Setter
public class DiscordAppender extends AppenderBase<ILoggingEvent> {

  private String webhookUrl;
  private String level = "ERROR"; // 只記錄 ERROR 以上
  private RestTemplate restTemplate = new RestTemplate();
  private BlockingQueue<ILoggingEvent> eventQueue = new LinkedBlockingQueue<>();
  private ExecutorService executor = Executors.newSingleThreadExecutor();

  @Override
  public void start() {
    super.start();
    // 啟動異步處理線程，避免阻塞主程序
    executor.submit(this::processEvents);
  }

  @Override
  protected void append(ILoggingEvent event) {
    // 只處理指定級別以上的日誌
    if (event.getLevel().isGreaterOrEqual(ch.qos.logback.classic.Level.toLevel(level))) {
      // 只處理指定級別以上的日誌
      if (event.getLevel().isGreaterOrEqual(ch.qos.logback.classic.Level.toLevel(level))) {
        boolean offered = eventQueue.offer(event);
        if (!offered) {
          // 隊列已滿時的處理策略
          System.err.println("Discord log 隊列已滿，事件已丟棄：" + event.getFormattedMessage());
        }
      }

    }
  }

  private void processEvents() {
    while (!Thread.currentThread().isInterrupted()) {
      try {
        ILoggingEvent event = eventQueue.take();
        sendToDiscord(event);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      }
    }
  }

  private void sendToDiscord(ILoggingEvent event) {
    try {
      // 檢查 webhookUrl 是否有效
      if (webhookUrl == null || webhookUrl.trim().isEmpty() || !webhookUrl.startsWith("http")) {
        // 避免重複輸出無意義的錯誤
        if (webhookUrl == null || webhookUrl.trim().isEmpty()) {
          System.err.println("Discord webhook URL 未設置，跳過日誌發送");
        } else {
          System.err.println("無效的 Discord webhook URL: " + webhookUrl);
        }
        return;
      }
      
      Map<String, Object> payload = new HashMap<>();

      // Discord Embed 格式
      Map<String, Object> embed = new HashMap<>();
      embed.put("title", "🚨 Bot 錯誤警報");
      embed.put("description", String.format("```\n%s\n```", event.getFormattedMessage()));
      embed.put("color", getColorByLevel(event.getLevel().toString()));
      embed.put("timestamp", java.time.Instant.ofEpochMilli(event.getTimeStamp()).toString());

      // 添加額外資訊
      Map<String, Object> field1 = new HashMap<>();
      field1.put("name", "級別");
      field1.put("value", event.getLevel().toString());
      field1.put("inline", true);

      Map<String, Object> field2 = new HashMap<>();
      field2.put("name", "Logger");
      field2.put("value", event.getLoggerName());
      field2.put("inline", true);

      embed.put("fields", java.util.List.of(field1, field2));

      payload.put("embeds", java.util.List.of(embed));

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);

      HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
      restTemplate.postForEntity(webhookUrl, request, String.class);

    } catch (Exception e) {
      // 避免無限循環，不要用 logger
      System.err.println("發送 Discord 日誌失敗: " + e.getMessage());
      e.printStackTrace(); // 打印堆疊追蹤以提供更多調試信息
    }
  }

  private Color getColorByLevel(String level) {
    return switch (level) {
      case "ERROR" -> ColorEnums.RED.getColor();  // 紅色
      case "WARN" -> ColorEnums.ORANGE.getColor();   // 橘色
      case "INFO" -> ColorEnums.GREEN.getColor();   // 綠色
      default -> ColorEnums.PURPLE.getColor();       // 紫色
    };
  }

  @Override
  public void stop() {
    executor.shutdown();
    super.stop();
  }
}