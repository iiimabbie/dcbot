package per.iiimabbie.discordbot.service;

import per.iiimabbie.discordbot.model.ChatMessage;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * AI 服務接口，用於生成AI回應
 */
public interface AiService {
  /**
   * 根據聊天歷史生成AI回應
   * @param chatHistory 聊天歷史消息列表
   * @return 異步回應結果
   */
  CompletableFuture<String> generateResponse(List<ChatMessage> chatHistory);
}