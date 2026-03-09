package cz.cyberrange.platform.training.opensearch.model;


import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.xml.validation.Schema;


/**
 * This class have to be extended when some event should be saved to OpenSearch. It provides 2
 * member variables 'timestamp' and 'type': 'timestamp' is generated based on current time 'type' is
 * generated based on your pojoClass (name of package + class name)
 */
@ApiModel(
        value = "Parent class for all audit POJO classes",
    description =
        "This class have to be extended when some event should be saved to OpenSearch."
            + " It provides 2 member variables 'timestamp' and 'type': 'timestamp' is generated based on current time 'type'"
            + " is generated based on your pojoClass (name of package + class name).")
@JsonPropertyOrder({"type", "timestamp"})
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type",
    visible = true)
@JsonSubTypes({
  @JsonSubTypes.Type(
      value = AssessmentAnswers.class,
      name = "cz.cyberrange.platform.events.trainings.AssessmentAnswers"),
  @JsonSubTypes.Type(
      value = CorrectAnswerSubmitted.class,
      name = "cz.cyberrange.platform.events.trainings.CorrectFlagSubmitted"),
  @JsonSubTypes.Type(
      value = CorrectAnswerSubmitted.class,
      name = "cz.cyberrange.platform.events.trainings.CorrectAnswerSubmitted"),
  @JsonSubTypes.Type(
      value = CorrectPasskeySubmitted.class,
      name = "cz.cyberrange.platform.events.trainings.CorrectPasskeySubmitted"),
  @JsonSubTypes.Type(
      value = HintTaken.class,
      name = "cz.cyberrange.platform.events.trainings.HintTaken"),
  @JsonSubTypes.Type(
      value = LevelCompleted.class,
      name = "cz.cyberrange.platform.events.trainings.LevelCompleted"),
  @JsonSubTypes.Type(
      value = LevelStarted.class,
      name = "cz.cyberrange.platform.events.trainings.LevelStarted"),
  @JsonSubTypes.Type(
      value = SolutionDisplayed.class,
      name = "cz.cyberrange.platform.events.trainings.SolutionDisplayed"),
  @JsonSubTypes.Type(
      value = TrainingRunEnded.class,
      name = "cz.cyberrange.platform.events.trainings.TrainingRunEnded"),
  @JsonSubTypes.Type(
      value = TrainingRunResumed.class,
      name = "cz.cyberrange.platform.events.trainings.TrainingRunResumed"),
  @JsonSubTypes.Type(
      value = TrainingRunStarted.class,
      name = "cz.cyberrange.platform.events.trainings.TrainingRunStarted"),
  @JsonSubTypes.Type(
      value = WrongAnswerSubmitted.class,
      name = "cz.cyberrange.platform.events.trainings.WrongFlagSubmitted"),
  @JsonSubTypes.Type(
      value = WrongAnswerSubmitted.class,
      name = "cz.cyberrange.platform.events.trainings.WrongAnswerSubmitted"),
  @JsonSubTypes.Type(
      value = WrongPasskeySubmitted.class,
      name = "cz.cyberrange.platform.events.trainings.WrongPasskeySubmitted")
})
@SuperBuilder
@Getter
public abstract class AbstractAuditPOJO {

  @JsonProperty(value = "sandbox_id", required = true)
  protected String sandboxId;

  @JsonProperty(value = "pool_id", required = true)
  protected Long poolId;

  @JsonProperty(value = "training_definition_id", required = true)
  protected long trainingDefinitionId;

  @JsonProperty(value = "training_instance_id", required = true)
  protected long trainingInstanceId;

  @JsonProperty(value = "training_run_id", required = true)
  protected long trainingRunId;

  // The time in particular training run (in particular training)
  @JsonProperty(value = "training_time", required = true)
  @JsonAlias("game_time")
  protected long trainingTime;

  // Actual score of the player in the level
  @JsonProperty(value = "actual_score_in_level", required = true)
  protected int actualScoreInLevel;

  // ID for the training run level that is generated when the training definition with levels is created or uploaded
  @JsonProperty(value = "level", required = true)
  protected long level;

  // Order of the level in the training definition
  @JsonProperty(value = "level_order", required = true)
  protected long levelOrder;

  // Id of player in the training run
  @JsonProperty("user_ref_id")
  protected long userRefId;

  // The time at which the event occurred
  @JsonProperty(value = "timestamp", required = true)
  @Setter
  protected long timestamp;

  // Type of event
  @JsonProperty(value = "type", required = true)
  @Setter
  protected String type;

  // Total score of the player achieved in the training levels
  @JsonProperty(value = "total_training_level_score", required = true)
  @JsonAlias("total_game_level_score")
  private int totalTrainingScore;

  // Total score of the player achieved in the assessment levels
  @JsonProperty(value = "total_assessment_level_score", required = true)
  private int totalAssessmentScore;

  /** Instantiates a new Abstract audit pojo. */
  protected AbstractAuditPOJO() {}

  /**
   * Instantiates a new Abstract audit pojo.
   *
   * @param timestamp the timestamp
   * @param type the type
   */
  protected AbstractAuditPOJO(long timestamp, String type) {
    this.timestamp = timestamp;
    this.type = type;
  }

  @Override
  public String toString() {
    return "AbstractAuditPOJO [timestamp=" + timestamp + ", type=" + type + "]";
  }
}
