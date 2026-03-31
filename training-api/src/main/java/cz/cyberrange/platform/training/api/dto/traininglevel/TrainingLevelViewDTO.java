package cz.cyberrange.platform.training.api.dto.traininglevel;

import cz.cyberrange.platform.training.api.dto.AbstractLevelDTO;
import cz.cyberrange.platform.training.api.dto.hint.HintForTrainingLevelViewDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/** Encapsulates basic information about training level. */
@Getter
@Setter
@ToString
@ApiModel(
    value = "TrainingLevelViewDTO",
    description = "An assignment containing security tasks whose completion yields a answer.",
    parent = AbstractLevelDTO.class)
public class TrainingLevelViewDTO extends AbstractLevelDTO {

  @ApiModelProperty(
      value = "The information and experiences that are directed towards a participant.",
      example = "Play me")
  private String content;

  @ApiModelProperty(value = "Sign if displaying of solution is penalized.", example = "true")
  private boolean solutionPenalized;

  @ApiModelProperty(
      value = "Estimated time (minutes) taken by the player to solve the level.",
      example = "25")
  private int estimatedDuration;

  @ApiModelProperty(
      value = "How many times player can submit incorrect answer before displaying solution.",
      example = "5")
  private int incorrectAnswerLimit;

  @ApiModelProperty(value = "Information which helps player resolve the level.")
  private Set<HintForTrainingLevelViewDTO> hints = new HashSet<>();
}
