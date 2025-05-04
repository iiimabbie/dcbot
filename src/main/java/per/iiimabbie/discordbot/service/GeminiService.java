package per.iiimabbie.discordbot.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import per.iiimabbie.discordbot.model.ChatMessage;
import per.iiimabbie.discordbot.util.ConfigLoader;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 使用 Google Gemini API 的 AI 服務實現
 */
public class GeminiService implements AiService {

  private static final Logger logger = LoggerFactory.getLogger(GeminiService.class);

  private final String apiKey;
  private final HttpClient httpClient;
  private final String systemPrompt;
  private final static String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
  private static final int MAX_RETRIES = 3;
  private final String botName = ConfigLoader.get("bot.name");

  public GeminiService(String apiKey) {
    this.apiKey = apiKey;
    this.httpClient = HttpClient.newHttpClient();
    // 從配置檔讀取系統提示
    this.systemPrompt = ConfigLoader.get("bot.system.prompt");
  }

  @Override
  public CompletableFuture<String> generateResponse(List<ChatMessage> chatHistory) {
    try {
      // 構建 Gemini API 請求
      JSONObject requestBody = buildRequestBody(chatHistory);

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
      return sendWithRetry(request, 0);

    } catch (Exception e) {
      logger.error("建立請求時發生錯誤: {}", e.getMessage(), e);
      CompletableFuture<String> future = new CompletableFuture<>();
      future.completeExceptionally(e);
      return future;
    }
  }

  private CompletableFuture<String> sendWithRetry(HttpRequest request, int attempt) {
    return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
        .thenCompose(response -> {
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
                return CompletableFuture.completedFuture(
                    "抱歉，我遇到了編碼問題。請嘗試簡單的英文提問，或聯繫管理員檢查系統編碼設置。");
              }

              return CompletableFuture.completedFuture(result);
            } catch (Exception e) {
              logger.error("解析回應失敗: {}", e.getMessage(), e);
              return CompletableFuture.completedFuture("解析 AI 回應時發生錯誤，請查看log。");
            }
          } else {
            // 嘗試重試
            if (attempt < MAX_RETRIES) {
              logger.warn("API 呼叫失敗，狀態碼: {}，嘗試重試 ({}/{})",
                  response.statusCode(), attempt + 1, MAX_RETRIES);
              // 延遲一段時間後重試
              try {
                Thread.sleep(3000L * (attempt + 1)); // 指數退避
              } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
              }
              return sendWithRetry(request, attempt + 1);
            } else {
              // 處理錯誤
              logger.error("API 呼叫失敗，狀態碼: {}，回應: {}", response.statusCode(), response.body());
              CompletableFuture<String> future = new CompletableFuture<>();
              future.completeExceptionally(new RuntimeException(
                  "API 呼叫失敗，狀態碼: " + response.statusCode() + "\n回應: " + response.body()));
              return future;
            }
          }
        });
  }

  private JSONObject buildRequestBody(List<ChatMessage> chatHistory) {
    JSONObject requestBody = new JSONObject();

    // 每次對話都添加系統提示作為第一條消息，確保人設不丟失
    JSONArray contents = new JSONArray();

    // 添加固定的系統提示作為第一條用戶消息
    JSONObject systemMessage = new JSONObject();
    systemMessage.put("role", "user");

    JSONArray systemParts = new JSONArray();
    JSONObject systemPart = new JSONObject();
    systemPart.put("text", "systemPrompt: " + systemPrompt);
    systemParts.put(systemPart);

    systemMessage.put("parts", systemParts);
    contents.put(systemMessage);

    // 添加 AI 的確認回應
    JSONObject aiConfirmation = new JSONObject();
    aiConfirmation.put("role", "model");

    JSONArray aiParts = new JSONArray();
    JSONObject aiPart = new JSONObject();
    aiPart.put("text", "好的，我是" + botName + "，我會按照指示與用戶互動。");
    aiParts.put(aiPart);

    aiConfirmation.put("parts", aiParts);
    contents.put(aiConfirmation);

    // 然後添加實際的對話歷史
    for (ChatMessage message : chatHistory) {
      JSONObject content = new JSONObject();

      String role = message.role();
      content.put("role", role);

      JSONArray parts = new JSONArray();
      JSONObject part = new JSONObject();
      // 如果是用戶訊息，加上用戶名
      if (role.equals("user")) {
        part.put("text", message.userName() + ": " + message.content());
      } else {
        part.put("text", message.content());
      }

      parts.put(part);
      content.put("parts", parts);
      contents.put(content);
    }

    requestBody.put("contents", contents);

    // AI參數
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
    return requestBody;
  }
}