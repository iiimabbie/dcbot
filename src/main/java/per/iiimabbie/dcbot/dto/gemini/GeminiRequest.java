package per.iiimabbie.dcbot.dto.gemini;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Gemini API 請求資料模型
 *
 * @author iiimabbie
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeminiRequest {

  @JsonProperty("contents")
  private List<Content> contents;

  @JsonProperty("generationConfig")
  private GenerationConfig generationConfig;

  @JsonProperty("safetySettings")
  private List<SafetySetting> safetySettings;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Content {

    @JsonProperty("parts")
    private List<Part> parts;

    @JsonProperty("role")
    private String role; // "user" 或 "model"
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Part {

    @JsonProperty("text")
    private String text;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class GenerationConfig {

    @JsonProperty("temperature")
    private Double temperature;

    @JsonProperty("topK")
    private Integer topK;

    @JsonProperty("topP")
    private Double topP;

    @JsonProperty("maxOutputTokens")
    private Integer maxOutputTokens;

    @JsonProperty("stopSequences")
    private List<String> stopSequences;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class SafetySetting {

    @JsonProperty("category")
    private String category;

    @JsonProperty("threshold")
    private String threshold;
  }
}