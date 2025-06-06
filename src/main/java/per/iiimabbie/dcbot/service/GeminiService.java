package per.iiimabbie.dcbot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import per.iiimabbie.dcbot.config.BotConfig;
import per.iiimabbie.dcbot.config.GeminiConfig;
import per.iiimabbie.dcbot.dto.gemini.GeminiRequest;
import per.iiimabbie.dcbot.dto.gemini.GeminiResponse;
import per.iiimabbie.dcbot.exception.BotException;

/**
 * Gemini API 服務
 * 負責處理與 Google Gemini AI 的所有交互
 *
 * @author iiimabbie
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiService {

  private final GeminiConfig geminiConfig;
  private final BotConfig botConfig;
  // 使用配置好的 RestTemplate, 需配合lombok.config
  @Qualifier("defaultRestTemplate")
  private final RestTemplate restTemplate;

  // 設定 ObjectMapper 忽略未知欄位
  private final ObjectMapper objectMapper = new ObjectMapper()
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  // 最大歷史訊息數量 - 避免 token 超限
  private static final int MAX_HISTORY_COUNT = 50;

  /**
   * 處理 Discord 訊息並取得 AI 回應
   *
   * @param channel        Discord 頻道
   * @param currentMessage 當前訊息
   * @return AI 回應文字
   * @throws BotException 當處理過程中發生錯誤時拋出
   */
  public String processMessage(MessageChannel channel, Message currentMessage) {
    try {
      // 1. 建立對話歷史
      List<GeminiRequest.Content> contents = buildConversationHistory(channel, currentMessage);

      // 2. 建立請求
      GeminiRequest request = GeminiRequest.builder()
          .contents(contents)
          .generationConfig(buildGenerationConfig())
          .safetySettings(buildSafetySettings())
          .build();

      // 3. 發送請求並返回結果
      return sendGeminiRequest(request);

    } catch (BotException e) {
      // 重新拋出已知的業務異常
      throw e;
    } catch (JsonProcessingException e) {
      log.error("JSON 處理失敗", e);
      throw BotException.geminiError("API 請求格式錯誤", e);
    } catch (Exception e) {
      log.error("處理 Gemini 請求時發生未知錯誤", e);
      throw BotException.geminiError("AI 服務暫時無法使用", e);
    }
  }

  /**
   * 建立對話歷史
   * 包含 system prompt + model 確認 + 歷史訊息 + 當前訊息
   */
  private List<GeminiRequest.Content> buildConversationHistory(MessageChannel channel, Message currentMessage) {
    List<GeminiRequest.Content> contents = new ArrayList<>();

    try {
      // 1. 先加入 system prompt（固定在第一位）
      if (botConfig.getSystemPrompt() != null && !botConfig.getSystemPrompt().trim().isEmpty()) {
        contents.add(GeminiRequest.Content.builder()
            .role("user")
            .parts(List.of(GeminiRequest.Part.builder()
                .text("System: " + botConfig.getSystemPrompt())
                .build()))
            .build());
      }

      // Model 確認回應 (model 角色)
      contents.add(GeminiRequest.Content.builder()
          .role("model")
          .parts(List.of(GeminiRequest.Part.builder()
              .text("好的，我是" + botConfig.getName() + "，我會按照指示與用戶互動。")
              .build()))
          .build());

      // 2. 取得歷史訊息
      try {
        MessageHistory history = channel.getHistoryBefore(currentMessage, MAX_HISTORY_COUNT).complete();
        List<Message> messages = new ArrayList<>(history.getRetrievedHistory());

        // 反轉順序，讓最舊的訊息在前面
        Collections.reverse(messages);

        // 3. 處理歷史訊息
        for (Message msg : messages) {
          // 跳過系統訊息和空訊息
          if (msg.getAuthor().isSystem() || msg.getContentRaw().trim().isEmpty()) {
            continue;
          }

          String messageContent = cleanMessageContent(msg);
          if (messageContent.isEmpty()) {
            continue;
          }

          // 判斷是機器人還是用戶
          String role = msg.getAuthor().isBot() ? "model" : "user";

          contents.add(GeminiRequest.Content.builder()
              .role(role)
              .parts(List.of(GeminiRequest.Part.builder()
                  .text(messageContent)
                  .build()))
              .build());
        }

      } catch (Exception e) {
        log.warn("取得歷史訊息失敗，僅使用當前訊息: {}", e.getMessage());
        // 這裡不拋出異常，因為即使沒有歷史訊息也能繼續處理
      }

      // 4. 加入當前訊息
      String currentContent = cleanMessageContent(currentMessage);
      if (!currentContent.isEmpty()) {
        contents.add(GeminiRequest.Content.builder()
            .role("user")
            .parts(List.of(GeminiRequest.Part.builder()
                .text(currentContent)
                .build()))
            .build());
      }

      log.debug("建立了 {} 則對話內容", contents.size());
      return contents;

    } catch (Exception e) {
      log.error("建立對話歷史時發生錯誤", e);
      throw BotException.discordError("無法取得對話歷史", e);
    }
  }

  /**
   * 清理訊息內容
   * 移除 @ 標記、多餘空白等
   */
  private String cleanMessageContent(Message message) {
    String content = message.getContentDisplay();

    // 如果訊息太短就跳過
    if (content.isEmpty()) {
      return "";
    }

    // 加上用戶名稱前綴（讓 AI 知道是誰說的）
    String username = message.getAuthor().getName();
    if (!message.getAuthor().isBot()) {
      content = username + ": " + content;
    }

    return content;
  }

  /**
   * 發送 Gemini API 請求
   *
   * @param request Gemini 請求物件
   * @return AI 回應文字
   * @throws BotException 當 API 調用失敗時拋出
   */
  private String sendGeminiRequest(GeminiRequest request) throws JsonProcessingException {
    String url = geminiConfig.getApi().getUrl() + "?key=" + geminiConfig.getApi().getKey();

    // 建立 HTTP 標頭
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    // 序列化請求
    String requestJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(request);
    log.debug("Gemini 請求 JSON: {}", requestJson);

    HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

    try {
      // 發送請求
      ResponseEntity<String> response = restTemplate.exchange(
          url, HttpMethod.POST, entity, String.class);

      if (!response.getStatusCode().is2xxSuccessful()) {
        throw BotException.geminiError("Gemini API 回應錯誤: " + response.getStatusCode());
      }

      log.debug("Gemini API 原始回應: {}", response.getBody());

      // 解析回應
      GeminiResponse geminiResponse = objectMapper.readValue(
          response.getBody(), GeminiResponse.class);

      String responseText = geminiResponse.getFirstCandidateText();
      if (responseText == null || responseText.trim().isEmpty()) {
        log.warn("Gemini 回應為空，使用預設訊息");
        return "抱歉，我沒有收到有效的回應...";
      }

      log.info("Gemini 回應: {}", responseText.replaceAll("\\R", "\\\\n"));
      return responseText;

    } catch (BotException e) {
      // 重新拋出自己的異常
      throw e;
    } catch (Exception e) {
      log.error("調用 Gemini API 時發生網路錯誤", e);
      throw BotException.networkError("網路連線問題", e);
    }
  }

  /**
   * 建立生成配置
   */
  private GeminiRequest.GenerationConfig buildGenerationConfig() {
    return GeminiRequest.GenerationConfig.builder()
        .temperature(0.7)  // 創意程度
        .topK(40)         // Top-K 採樣
        .topP(0.95)       // Top-P 採樣
        .maxOutputTokens(2048)  // 最大輸出長度
        .build();
  }

  /**
   * 建立安全設定
   */
  private List<GeminiRequest.SafetySetting> buildSafetySettings() {
    return List.of(
        GeminiRequest.SafetySetting.builder()
            .category("HARM_CATEGORY_HARASSMENT") // 防止騷擾內容
            .threshold("BLOCK_MEDIUM_AND_ABOVE")
            .build(),
        GeminiRequest.SafetySetting.builder()
            .category("HARM_CATEGORY_HATE_SPEECH") // 防止仇恨言論
            .threshold("BLOCK_MEDIUM_AND_ABOVE")
            .build(),
        GeminiRequest.SafetySetting.builder()
            .category("HARM_CATEGORY_SEXUALLY_EXPLICIT") // 防止性露骨內容
            .threshold("BLOCK_MEDIUM_AND_ABOVE")
            .build(),
        GeminiRequest.SafetySetting.builder()
            .category("HARM_CATEGORY_DANGEROUS_CONTENT") // 防止危險內容
            .threshold("BLOCK_MEDIUM_AND_ABOVE")
            .build()
    );
    /*
    Gemini API 提供的閾值選項有：
    BLOCK_NONE：不阻止任何內容
    BLOCK_LOW_AND_ABOVE：阻止輕微及以上程度的內容
    BLOCK_MEDIUM_AND_ABOVE：阻止中等及以上程度的內容
    BLOCK_HIGH_AND_ABOVE：只阻止高度及以上程度的內容
    BLOCK_ONLY_MAXIMUM：只阻止最高程度的內容
    */
  }
}