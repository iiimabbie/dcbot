package per.iiimabbie.dcbot.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import per.iiimabbie.dcbot.config.BotConfig;
import per.iiimabbie.dcbot.config.DiscordConfig;
import per.iiimabbie.dcbot.config.GeminiConfig;

/**
 * 配置驗證服務 - 在應用啟動時驗證必要配置
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigValidationService {

  private final DiscordConfig discordConfig;
  private final GeminiConfig geminiConfig;
  private final BotConfig botConfig;

  @PostConstruct
  public void validateConfigurations() {
    log.info("開始驗證應用配置...");

    try {
      validateDiscordConfig();
      validateGeminiConfig();
      validateBotConfig();

      log.info("所有配置驗證通過");

    } catch (ConfigurationException e) {
      log.error("配置驗證失敗: {}", e.getMessage());
      // 直接終止應用
      System.exit(1);
    }
  }

  private void validateDiscordConfig() throws ConfigurationException {
    if (isBlank(discordConfig.getToken())) {
      throw new ConfigurationException("Discord Token 未設定 (DISCORD_TOKEN)");
    }

    // 檢查 Token 格式 (Discord Token 通常以 Bot 或 數字開頭)
    if (!isValidDiscordToken(discordConfig.getToken())) {
      throw new ConfigurationException("Discord Token 格式不正確");
    }

    if (discordConfig.getGuild() == null || isBlank(discordConfig.getGuild().getId())) {
      log.warn("Guild ID 未設定，部分功能可能無法使用");
    } else {
      // 檢查 Guild ID 格式 (應該是純數字)
      if (!discordConfig.getGuild().getId().matches("\\d+")) {
        throw new ConfigurationException("Guild ID 格式不正確，應為純數字");
      }
    }
  }

  private void validateGeminiConfig() throws ConfigurationException {
    if (geminiConfig.getApi() == null) {
      throw new ConfigurationException("Gemini API 配置未設定");
    }

    if (isBlank(geminiConfig.getApi().getKey())) {
      throw new ConfigurationException("Gemini API Key 未設定 (GEMINI_API_KEY)");
    }

    if (isBlank(geminiConfig.getApi().getUrl())) {
      throw new ConfigurationException("Gemini API URL 未設定 (GEMINI_API_URL)");
    }

    // 檢查 API Key 格式 (Google API Key 通常以 AIza 開頭)
    if (!geminiConfig.getApi().getKey().startsWith("AIza")) {
      log.warn("Gemini API Key 格式可能不正確 (應以 AIza 開頭)");
    }
  }

  private void validateBotConfig() throws ConfigurationException {
    if (isBlank(botConfig.getName())) {
      throw new ConfigurationException("Bot 名稱未設定 (BOT_NAME)");
    }

    if (botConfig.getStatus() == null || isBlank(botConfig.getStatus().getText())) {
      log.warn("Bot 狀態文字未設定，將使用預設值");
    }

    if (isBlank(botConfig.getSystemPrompt())) {
      log.warn("System Prompt 未設定，AI 行為可能不符預期");
    }

    if (isBlank(botConfig.getOwnerId())) {
      log.warn("Bot Owner ID 未設定，Owner 指令將無法使用");
    } else {
      // 檢查 Owner ID 格式
      if (!botConfig.getOwnerId().matches("\\d+")) {
        throw new ConfigurationException("Bot Owner ID 格式不正確，應為純數字");
      }
    }
  }

  private boolean isValidDiscordToken(String token) {
    // Discord Token 基本格式驗證
    return token.matches("^[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+$");
  }

  private boolean isBlank(String str) {
    return str == null || str.trim().isEmpty();
  }

  /**
   * 配置異常
   */
  public static class ConfigurationException extends Exception {
    public ConfigurationException(String message) {
      super(message);
    }
  }
}