package cz.cyberrange.platform.training.api.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

public class VariantAnswer {

  @ApiModelProperty(
      value = "The content of the variant answer in particular (phase/level)",
      example = "nmap 192.168.0.1")
  @JsonProperty("answer_content")
  private String answerContent;

  @ApiModelProperty(value = "The identifier of the variant answer", example = "sandbox-1-2-answer")
  @JsonProperty("answer_variable_name")
  private String answerVariableName;

  public String getAnswerContent() {
    return answerContent;
  }

  public void setAnswerContent(String answerContent) {
    this.answerContent = answerContent;
  }

  public String getAnswerVariableName() {
    return answerVariableName;
  }

  public void setAnswerVariableName(String answerVariableName) {
    this.answerVariableName = answerVariableName;
  }

  @Override
  public String toString() {
    return "VariantAnswer{"
        + "answerContent='"
        + answerContent
        + '\''
        + ", answerVariableName='"
        + answerVariableName
        + '\''
        + '}';
  }
}
