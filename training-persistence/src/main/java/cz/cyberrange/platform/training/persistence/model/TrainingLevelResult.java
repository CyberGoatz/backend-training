package cz.cyberrange.platform.training.persistence.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * Persisted per-level result summary for a training run.
 */
@Entity
@Table(
    name = "training_level_result",
    uniqueConstraints = @UniqueConstraint(columnNames = {"training_run_id", "level_id"}))
public class TrainingLevelResult extends AbstractEntity<Long> {

  @Column(name = "training_run_id", nullable = false)
  private Long trainingRunId;

  @Column(name = "level_id", nullable = false)
  private Long levelId;

  @Column(name = "participant_level_score", nullable = false)
  private int participantLevelScore;

  @Column(name = "wrong_answers", nullable = false)
  private int wrongAnswers;

  @Column(name = "hints_taken", nullable = false)
  private int hintsTaken;

  @Column(name = "solution_taken", nullable = false)
  private boolean solutionTaken;

  @Column(name = "completed", nullable = false)
  private boolean completed;

  public Long getTrainingRunId() {
    return trainingRunId;
  }

  public void setTrainingRunId(Long trainingRunId) {
    this.trainingRunId = trainingRunId;
  }

  public Long getLevelId() {
    return levelId;
  }

  public void setLevelId(Long levelId) {
    this.levelId = levelId;
  }

  public int getParticipantLevelScore() {
    return participantLevelScore;
  }

  public void setParticipantLevelScore(int participantLevelScore) {
    this.participantLevelScore = participantLevelScore;
  }

  public int getWrongAnswers() {
    return wrongAnswers;
  }

  public void setWrongAnswers(int wrongAnswers) {
    this.wrongAnswers = wrongAnswers;
  }

  public void incrementWrongAnswers() {
    this.wrongAnswers++;
  }

  public int getHintsTaken() {
    return hintsTaken;
  }

  public void setHintsTaken(int hintsTaken) {
    this.hintsTaken = hintsTaken;
  }

  public void incrementHintsTaken() {
    this.hintsTaken++;
  }

  public boolean isSolutionTaken() {
    return solutionTaken;
  }

  public void setSolutionTaken(boolean solutionTaken) {
    this.solutionTaken = solutionTaken;
  }

  public boolean isCompleted() {
    return completed;
  }

  public void setCompleted(boolean completed) {
    this.completed = completed;
  }
}
