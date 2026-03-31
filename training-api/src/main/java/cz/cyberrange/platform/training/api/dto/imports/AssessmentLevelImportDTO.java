package cz.cyberrange.platform.training.api.dto.imports;

import cz.cyberrange.platform.training.api.dto.assessmentlevel.question.QuestionDTO;
import cz.cyberrange.platform.training.api.enums.AssessmentType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/** Encapsulates information about assessment level. Inherits from {@link AbstractLevelImportDTO} */
@Getter
@Setter
@ToString
@ApiModel(
    value = "AssessmentLevelImportDTO",
    description = "Imported assessment level.",
    parent = AbstractLevelImportDTO.class)
public class AssessmentLevelImportDTO extends AbstractLevelImportDTO {

  @ApiModelProperty(value = "Questions of assessment level to update.")
  @NotNull(message = "{assessmentLevel.questions.NotNull.message}")
  private List<QuestionDTO> questions = new ArrayList<>();

  @ApiModelProperty(
      value = "Instructions of assessment level to update.",
      example = "Fill me up slowly")
  @NotNull(message = "{assessmentLevel.instructions.NotNull.message}")
  private String instructions;

  @ApiModelProperty(
      value = "Type of assessment level to update.",
      required = true,
      example = "TEST")
  @NotNull(message = "{assessmentLevel.type.NotNull.message}")
  private AssessmentType assessmentType;
}
