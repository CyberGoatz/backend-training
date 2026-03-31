package cz.cyberrange.platform.training.api.dto.visualization.assessment.answer;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@ApiModel(
    value = "EMIAnswerDTO",
    description = "Holds an answer data about the specific EMI statement.")
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Getter
@Setter
public class EMIAnswerDTO extends AbstractAnswerDTO {

  @ApiModelProperty(value = "List of all available options for EMI statement.")
  private List<EMIOptionDTO> options;
}
