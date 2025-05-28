package per.iiimabbie.dcbot.dto.gemini;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Gemini API 回應資料模型
 *
 * @author iiimabbie
 */
@Data
public class GeminiResponse {

  @JsonProperty("candidates")
  private List<Candidate> candidates;

  @JsonProperty("usageMetadata")
  private UsageMetadata usageMetadata;

  @Data
  public static class Candidate {

    @JsonProperty("content")
    private Content content;

    @JsonProperty("finishReason")
    private String finishReason;

    @JsonProperty("index")
    private Integer index;

    @JsonProperty("safetyRatings")
    private List<SafetyRating> safetyRatings;
  }

  @Data
  public static class Content {

    @JsonProperty("parts")
    private List<Part> parts;

    @JsonProperty("role")
    private String role;
  }

  @Data
  public static class Part {

    @JsonProperty("text")
    private String text;
  }

  @Data
  public static class SafetyRating {

    @JsonProperty("category")
    private String category;

    @JsonProperty("probability")
    private String probability;
  }

  @Data
  public static class UsageMetadata {

    @JsonProperty("promptTokenCount")
    private Integer promptTokenCount;

    @JsonProperty("candidatesTokenCount")
    private Integer candidatesTokenCount;

    @JsonProperty("totalTokenCount")
    private Integer totalTokenCount;
  }

  /**
   * 取得第一個候選回應的文字內容
   */
  public String getFirstCandidateText() {
    if (candidates != null && !candidates.isEmpty()) {
      Candidate firstCandidate = candidates.getFirst();
      if (firstCandidate.getContent() != null &&
          firstCandidate.getContent().getParts() != null &&
          !firstCandidate.getContent().getParts().isEmpty()) {
        return firstCandidate.getContent().getParts().getFirst().getText();
      }
    }
    return null;
  }
}