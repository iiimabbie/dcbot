package per.iiimabbie.discordbot.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class ConfigLoader {
  private static final Properties properties = new Properties();

  static {
    try (Reader reader = new InputStreamReader(new FileInputStream("config.properties"), StandardCharsets.UTF_8)) {
      properties.load(reader);
    } catch (IOException e) {
      throw new RuntimeException("讀取配置檔失敗: " + e.getMessage(), e);
    }
  }

  public static Properties get() {
    return properties;
  }

  public static String get(String key) {
    return properties.getProperty(key);
  }

  public static String getOrDefault(String key, String defaultValue) {
    return properties.getProperty(key, defaultValue);
  }

  public static void reload() {
    try (Reader reader = new InputStreamReader(new FileInputStream("config.properties"), StandardCharsets.UTF_8)) {
      properties.clear();
      properties.load(reader);
    } catch (IOException e) {
      throw new RuntimeException("重新讀取配置檔失敗: " + e.getMessage(), e);
    }
  }

}

