package cz.cyberrange.platform.training.api.dto.assessmentlevel.question;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ApiModel(value = "QuestionAnswerDTO")
public class QuestionAnswerDTO {

  @ApiModelProperty(value = "ID of answered question", example = "1")
  @NotNull(message = "{questionAnswer.questionId.NotNull.message}")
  private Long questionId;

  @ApiModelProperty(value = "Answers to the question", example = "[\"An answer\"]")
  private Set<String> answers;

  @ApiModelProperty(
      value = "Mapping of the answers to question of type extended matching items",
      example = "{ \"1\": [2, 3]")
  private Map<Integer, Integer> extendedMatchingPairs;
}
