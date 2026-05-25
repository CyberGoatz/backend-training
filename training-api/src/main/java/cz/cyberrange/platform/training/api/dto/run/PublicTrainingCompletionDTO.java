package cz.cyberrange.platform.training.api.dto.run;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import cz.cyberrange.platform.training.api.converters.LocalDateTimeUTCSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/** Public-safe summary of a completed training run. */
@Getter
@Setter
@ToString
@EqualsAndHashCode
@ApiModel(
    value = "PublicTrainingCompletionDTO",
    description = "Public-safe completed training summary.")
public class PublicTrainingCompletionDTO {

  @ApiModelProperty(value = "Main identifier of training run.", example = "1")
  private Long trainingRunId;

  @ApiModelProperty(value = "Associated training instance id.", example = "2")
  private Long trainingInstanceId;

  @ApiModelProperty(value = "Associated training definition id.", example = "3")
  private Long trainingDefinitionId;

  @ApiModelProperty(value = "Training instance title.", example = "Intro instance")
  private String trainingTitle;

  @ApiModelProperty(value = "Training definition title.", example = "Intro training")
  private String definitionTitle;

  @ApiModelProperty(value = "Date when the run finished.", example = "2022-10-19 10:23:54+02")
  @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
  private LocalDateTime finishedAt;

  @ApiModelProperty(value = "Score achieved in training levels.", example = "20")
  private Integer trainingScore;

  @ApiModelProperty(value = "Score achieved in assessment levels.", example = "5")
  private Integer assessmentScore;

  @ApiModelProperty(value = "Total score achieved.", example = "25")
  private Integer totalScore;

  @ApiModelProperty(value = "Maximum achievable score.", example = "30")
  private Integer maxScore;

  @ApiModelProperty(value = "Training time in milliseconds.", example = "3600000")
  private Long trainingTimeMs;
}
