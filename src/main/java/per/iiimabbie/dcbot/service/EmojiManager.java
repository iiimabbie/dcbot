package per.iiimabbie.dcbot.service;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.emoji.ApplicationEmoji;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import org.springframework.stereotype.Component;
import per.iiimabbie.dcbot.enums.BotEmojis;

@Component
@Slf4j
@RequiredArgsConstructor
public class EmojiManager {

  private final Map<String, ApplicationEmoji> emojiCache = new ConcurrentHashMap<>();
  private JDA jda;

  /**
   * 初始化時載入 emoji 快取
   * 需要在 JDA 準備好後呼叫
   */
  public void initialize(JDA jda) {
    this.jda = jda;
    reloadCache();
  }

  /**
   * 重新載入 emoji 快取
   */
  public void reloadCache() {
    if (jda == null) {
      log.warn("JDA 未初始化，無法載入 emoji 快取");
      return;
    }

    log.info("開始載入 Application Emoji 快取...");

    jda.retrieveApplicationEmojis().queue(emojis -> {
      emojiCache.clear();
      emojis.forEach(emoji -> emojiCache.put(emoji.getName(), emoji));

      log.info("載入了 {} 個 Application Emoji", emojis.size());
      emojis.forEach(emoji ->
          log.debug("Emoji: {} -> {}", emoji.getName(), emoji.getFormatted()));
    }, throwable -> {
      log.error("載入 emoji 快取失敗", throwable);
    });
  }

  /**
   * 取得 emoji 物件
   */
  public Optional<ApplicationEmoji> getEmoji(String name) {
    return Optional.ofNullable(emojiCache.get(name));
  }

  // 分類方法
  public String getToolEmoji(BotEmojis.Tool tool) {
    return getEmojiFormatted(tool.getName());
  }

  public String getEmotionEmoji(BotEmojis.Emotion emotion) {
    return getEmojiFormatted(emotion.getName());
  }

  public String getDiceEmoji(BotEmojis.Dice dice) {
    return getEmojiFormatted(dice.getName());
  }

  /**
   * 取得 emoji 格式化字串
   */
  public String getEmojiFormatted(String name) {
    return getEmoji(name)
        .map(ApplicationEmoji::getFormatted)
        .orElse("");
  }

  /**
   * 取得 Emoji 物件（用於按鈕等）
   */
  public Optional<Emoji> getEmojiObject(String name) {
    return getEmoji(name)
        .map(Emoji::fromCustom);
  }

  /**
   * 檢查是否有指定的 emoji
   */
  public boolean hasEmoji(String name) {
    return emojiCache.containsKey(name);
  }

  /**
   * 取得所有已快取的 emoji 名稱
   */
  public Set<String> getAllEmojiNames() {
    return new HashSet<>(emojiCache.keySet());
  }
}
