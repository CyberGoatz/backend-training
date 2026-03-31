package cz.cyberrange.platform.training.api.dto.visualization.assessment.answer;

import cz.cyberrange.platform.training.api.dto.UserRefDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@ApiModel(value = "MCQAnswerDTO", description = "Holds a data about the specific MCQ option.")
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Getter
@Setter
public class MCQAnswerDTO extends AbstractAnswerDTO {

  @ApiModelProperty(value = "Indicates the correctness of the MCQ option.", example = "false")
  private boolean isCorrect;

  @ApiModelProperty(value = "List of the participants that have chosen this MCQ option")
  private List<UserRefDTO> participants;
}
