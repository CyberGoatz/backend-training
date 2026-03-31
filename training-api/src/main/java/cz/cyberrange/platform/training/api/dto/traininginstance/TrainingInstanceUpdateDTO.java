package cz.cyberrange.platform.training.api.dto.traininginstance;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import cz.cyberrange.platform.training.api.converters.LocalDateTimeUTCDeserializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/** Encapsulates information about Training Instance, intended for edit of the instance. */
@Getter
@Setter
@ToString
@ApiModel(value = "TrainingInstanceUpdateDTO", description = "Training Instance to update.")
public class TrainingInstanceUpdateDTO {

  @ApiModelProperty(value = "Main identifier of training instance.", required = true, example = "2")
  @NotNull(message = "{traininginstanceupdate.id.NotNull.message}")
  private Long id;

  @ApiModelProperty(
      value = "Date when training instance starts.",
      required = true,
      example = "2019-10-19T10:28:02.727Z")
  @NotNull(message = "{traininginstanceupdate.startTime.NotNull.message}")
  @JsonDeserialize(using = LocalDateTimeUTCDeserializer.class)
  private LocalDateTime startTime;

  @ApiModelProperty(
      value = "Date when training instance ends.",
      required = true,
      example = "2019-10-25T10:28:02.727Z")
  @NotNull(message = "{traininginstanceupdate.endTime.NotNull.message}")
  @JsonDeserialize(using = LocalDateTimeUTCDeserializer.class)
  private LocalDateTime endTime;

  @ApiModelProperty(
      value = "Short textual description of the training instance.",
      required = true,
      example = "Current Instance")
  @NotEmpty(message = "{traininginstanceupdate.title.NotEmpty.message}")
  private String title;

  @ApiModelProperty(
      value = "AccessToken which will be modified and then used for accessing training run.",
      required = true,
      example = "hello-6578")
  @NotEmpty(message = "{traininginstanceupdate.accessToken.NotEmpty.message}")
  private String accessToken;

  @ApiModelProperty(
      value = "Reference to training definition from which is training instance created.",
      required = true,
      example = "1")
  @NotNull(message = "{traininginstanceupdate.trainingDefinition.NotNull.message}")
  private Long trainingDefinitionId;

  @ApiModelProperty(value = "Id of sandbox pool assigned to training instance", example = "1")
  private Long poolId;

  @ApiModelProperty(
      value = "Indicates if local sandboxes are used for training runs.",
      example = "true")
  private boolean localEnvironment;

  @ApiModelProperty(value = "Id of sandbox definition assigned to training instance", example = "1")
  private Long sandboxDefinitionId;

  @ApiModelProperty(
      value = "Sign if stepper bar should be displayed.",
      required = true,
      example = "true")
  private boolean showStepperBar;

  @ApiModelProperty(
      value =
          "Indicates if trainee can during training run move to the previous already solved levels.",
      example = "true")
  private boolean backwardMode;
}
