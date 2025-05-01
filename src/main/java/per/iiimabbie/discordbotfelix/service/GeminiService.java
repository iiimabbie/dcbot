package per.iiimabbie.discordbotfelix.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import per.iiimabbie.discordbotfelix.util.ConversationContext;

public class GeminiService {
  private static final Logger logger = LoggerFactory.getLogger(GeminiService.class);

  private final String apiKey;
  private final HttpClient httpClient;
  private String systemPrompt;
  private final static String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

  public GeminiService(String apiKey) {
    this.apiKey = apiKey;
    this.httpClient = HttpClient.newHttpClient();

    // 從配置檔讀取系統提示
    Properties properties = new Properties();
    try (InputStreamReader reader = new InputStreamReader(new FileInputStream("config.properties"), StandardCharsets.UTF_8)) {
      properties.load(new FileInputStream("config.properties"));
      this.systemPrompt = properties.getProperty("bot.system.prompt",
          "你是一個名叫「小幫手」的Discord機器人。" +
              "你的人設是：活潑可愛、知識淵博、語氣輕鬆但不失專業。" +
              "請用繁體中文回答，使用台灣的用語和表達方式。");
    } catch (IOException e) {
      // 如果讀取失敗，使用默認系統提示
      this.systemPrompt = "你是一個Discord助手機器人。請簡潔明瞭地回答問題。";
      logger.error("警告：無法讀取系統提示，使用默認提示。{}", e.getMessage());
    }
  }

  // 帶上下文的 Gemini API 調用
  public CompletableFuture<String> generateResponseWithContext(ConversationContext context) {
    try {
      // 構建 Gemini API 請求
      JSONObject requestBody = new JSONObject();

      // 每次對話都添加系統提示作為第一條消息，確保人設不丟失
      JSONArray contents = new JSONArray();

      // 添加固定的系統提示作為第一條用戶消息
      JSONObject systemMessage = new JSONObject();
      systemMessage.put("role", "user");

      JSONArray systemParts = new JSONArray();
      JSONObject systemPart = new JSONObject();
      systemPart.put("text", systemPrompt);
      systemParts.put(systemPart);

      systemMessage.put("parts", systemParts);
      contents.put(systemMessage);

      // 添加 AI 的確認回應
      JSONObject aiConfirmation = new JSONObject();
      aiConfirmation.put("role", "model");

      JSONArray aiParts = new JSONArray();
      JSONObject aiPart = new JSONObject();
      aiPart.put("text", "好的，我是Felix，我會按照指示與用戶互動。");
      aiParts.put(aiPart);

      aiConfirmation.put("parts", aiParts);
      contents.put(aiConfirmation);

      // 然後添加實際的對話歷史
      List<ConversationContext.Message> messages = context.getMessages();
      for (ConversationContext.Message message : messages) {
        JSONObject content = new JSONObject();

        String role = message.getRole().equals("model") ? "model" : "user";
        content.put("role", role);

        JSONArray parts = new JSONArray();
        JSONObject part = new JSONObject();
        part.put("text", message.getContent());
        parts.put(part);

        content.put("parts", parts);
        contents.put(content);
      }

      requestBody.put("contents", contents);

      // 可選的生成參數
      JSONObject generationConfig = new JSONObject();
      generationConfig.put("temperature", 0.7);
      generationConfig.put("maxOutputTokens", 800);
      generationConfig.put("topP", 0.95);
      generationConfig.put("topK", 40);
      requestBody.put("generationConfig", generationConfig);

      // 設置安全過濾
      JSONObject safetySettings = new JSONObject();
      safetySettings.put("category", "HARM_CATEGORY_HARASSMENT");
      safetySettings.put("threshold", "BLOCK_NONE");

      JSONArray safetySettingsArray = new JSONArray();
      safetySettingsArray.put(safetySettings);

      requestBody.put("safetySettings", safetySettingsArray);

      // 輸出請求內容用於調試
      logger.debug("請求 URL: {}", API_URL + "?key=" + apiKey);
      logger.debug("請求內容: {}", requestBody.toString(2));

      // 確保使用 UTF-8 編碼
      String requestBodyStr = requestBody.toString();
      byte[] requestBodyBytes = requestBodyStr.getBytes(StandardCharsets.UTF_8);

      // 建立 HTTP 請求，明確設定編碼
      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(API_URL + "?key=" + apiKey))
          .header("Content-Type", "application/json; charset=UTF-8")
          .POST(HttpRequest.BodyPublishers.ofByteArray(requestBodyBytes))
          .build();

      // 異步發送請求
      return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
          .thenApply(response -> {
            logger.info("回應狀態碼: {}", response.statusCode());

            if (response.statusCode() == 200) {
              JSONObject jsonResponse = new JSONObject(response.body());

              try {
                // 嘗試解析回應
                String result = jsonResponse
                    .getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text");

                // 檢查結果是否包含問號（可能的編碼問題）
                if (result.contains("??????")) {
                  logger.warn("檢測到可能的編碼問題，回應包含問號");
                  return "抱歉，我遇到了編碼問題。請嘗試簡單的英文提問，或聯繫管理員檢查系統編碼設置。";
                }

                return result;
              } catch (Exception e) {
                logger.error("解析回應失敗: {}", e.getMessage(), e);
                return "解析 AI 回應時發生錯誤，請查看日誌。";
              }
            } else {
              // 處理錯誤
              logger.error("API 呼叫失敗，狀態碼: {}，回應: {}", response.statusCode(), response.body());
              throw new RuntimeException("API 呼叫失敗，狀態碼: " + response.statusCode() + "\n回應: " + response.body());
            }
          });

    } catch (Exception e) {
      logger.error("建立請求時發生錯誤: {}", e.getMessage(), e);
      CompletableFuture<String> future = new CompletableFuture<>();
      future.completeExceptionally(e);
      return future;
    }
  }

  // 測試方法，可以直接運行來測試 Gemini API 是否正常工作
  public static void main(String[] args) {
    // 設置 JVM 的字符集
    System.setProperty("file.encoding", "UTF-8");

    // 從配置檔讀取 API Key
    Properties properties = new Properties();
    try {
      properties.load(new FileInputStream("config.properties"));
      String apiKey = properties.getProperty("gemini.api.key");

      if (apiKey == null || apiKey.isEmpty()) {
        logger.error("Gemini API Key 未設定");
        return;
      }

      // 創建服務並測試
      GeminiService service = new GeminiService(apiKey);
      ConversationContext context = new ConversationContext();
      context.addUserMessage("你好，請介紹一下自己");

      service.generateResponseWithContext(context).thenAccept(response -> {
        logger.info("=== AI 回應 ===");
        logger.info(response);
        logger.info("==============");
      }).join();

    } catch (IOException e) {
      logger.error("無法讀取配置檔: {}", e.getMessage(), e);
    }
  }
}