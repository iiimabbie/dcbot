package per.iiimabbie.discordbot.service;

import per.iiimabbie.discordbot.model.ChatMessage;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * AI 服務接口，用於生成 AI 回應。
 * 提供與 AI 模型交互的標準方法。
 *
 * @author iiimabbie
 */
public interface AiService {

  /**
   * 根據聊天歷史生成 AI 回應。
   * 實現此方法的類應該將聊天歷史轉換為適合 AI 模型的格式，
   * 然後調用 AI API 並處理返回結果。
   *
   * @param chatHistory 聊天歷史消息列表，按時間從新到舊排序
   * @return 包含 AI 回應的 CompletableFuture 對象
   * @throws IllegalArgumentException 如果聊天歷史為 null 或為空
   */
  CompletableFuture<String> generateResponse(List<ChatMessage> chatHistory);
}