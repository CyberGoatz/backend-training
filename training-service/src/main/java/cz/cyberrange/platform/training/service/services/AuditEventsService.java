package cz.cyberrange.platform.training.service.services;

import cz.cyberrange.platform.training.opensearch.events.training.logging.AuditService;
import cz.cyberrange.platform.training.opensearch.events.training.model.AbstractAuditPOJO;
import cz.cyberrange.platform.training.opensearch.events.training.model.AssessmentAnswered;
import cz.cyberrange.platform.training.opensearch.events.training.model.CorrectAnswerSubmitted;
import cz.cyberrange.platform.training.opensearch.events.training.model.HintTaken;
import cz.cyberrange.platform.training.opensearch.events.training.model.LevelCompleted;
import cz.cyberrange.platform.training.opensearch.events.training.model.LevelStarted;
import cz.cyberrange.platform.training.opensearch.events.training.model.SolutionDisplayed;
import cz.cyberrange.platform.training.opensearch.events.training.model.TrainingRunFinished;
import cz.cyberrange.platform.training.opensearch.events.training.model.TrainingRunResumed;
import cz.cyberrange.platform.training.opensearch.events.training.model.TrainingRunStarted;
import cz.cyberrange.platform.training.opensearch.events.training.model.WrongAnswerSubmitted;
import cz.cyberrange.platform.training.opensearch.events.training.model.enums.EventLevelType;
import cz.cyberrange.platform.training.persistence.model.AbstractLevel;
import cz.cyberrange.platform.training.persistence.model.AccessLevel;
import cz.cyberrange.platform.training.persistence.model.AssessmentLevel;
import cz.cyberrange.platform.training.persistence.model.Hint;
import cz.cyberrange.platform.training.persistence.model.InfoLevel;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.persistence.model.TrainingLevel;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** The type Audit events service. */
@Service
public class AuditEventsService {

  private final AuditService auditService;

  /**
   * Instantiates a new Audit events service.
   *
   * @param auditService the audit service
   */
  @Autowired
  public AuditEventsService(AuditService auditService) {
    this.auditService = auditService;
  }

  /**
   * Audit training run started action.
   *
   * @param trainingRun the training run
   */
  public void auditTrainingRunStartedAction(TrainingRun trainingRun) {
    TrainingRunStarted.TrainingRunStartedBuilder<?, ?> trainingRunStartedBuilder =
        (TrainingRunStarted.TrainingRunStartedBuilder<?, ?>)
            fillInCommonBuilderFields(trainingRun, TrainingRunStarted.builder());

    TrainingRunStarted trainingRunStarted = trainingRunStartedBuilder.trainingTime(0L).build();
    auditService.saveTrainingRunEvent(trainingRunStarted);
  }

  /**
   * Audit level started action.
   *
   * @param trainingRun the training run
   */
  public void auditLevelStartedAction(TrainingRun trainingRun) {
    LevelStarted.LevelStartedBuilder<?, ?> levelStartedBuilder =
        (LevelStarted.LevelStartedBuilder<?, ?>)
            fillInCommonBuilderFields(trainingRun, LevelStarted.builder());

    LevelStarted levelStarted =
        levelStartedBuilder
            .levelType(getLevelType(trainingRun.getCurrentLevel()))
            .maxScore(trainingRun.getCurrentLevel().getMaxScore())
            .levelTitle(trainingRun.getCurrentLevel().getTitle())
            .build();
    auditService.saveTrainingRunEvent(levelStarted, 2);
  }

  /**
   * Audit level completed action.
   *
   * @param trainingRun the training run
   */
  public void auditLevelCompletedAction(TrainingRun trainingRun) {
    LevelCompleted.LevelCompletedBuilder<?, ?> levelCompletedBuilder =
        (LevelCompleted.LevelCompletedBuilder<?, ?>)
            fillInCommonBuilderFields(trainingRun, LevelCompleted.builder());

    LevelCompleted levelCompleted =
        levelCompletedBuilder.levelType(getLevelType(trainingRun.getCurrentLevel())).build();
    auditService.saveTrainingRunEvent(levelCompleted, 1);
  }

  /**
   * Audit hint taken action.
   *
   * @param trainingRun the training run
   * @param hint the hint
   */
  public void auditHintTakenAction(TrainingRun trainingRun, Hint hint) {
    HintTaken.HintTakenBuilder<?, ?> hintTakenBuilder =
        (HintTaken.HintTakenBuilder<?, ?>)
            fillInCommonBuilderFields(trainingRun, HintTaken.builder());

    HintTaken hintTaken =
        hintTakenBuilder
            .hintId(hint.getId())
            .hintPenaltyPoints(hint.getHintPenalty())
            .hintTitle(hint.getTitle())
            .build();
    auditService.saveTrainingRunEvent(hintTaken);
  }

  /**
   * Audit solution displayed action.
   *
   * @param trainingRun the training run
   */
  public void auditSolutionDisplayedAction(TrainingRun trainingRun) {
    SolutionDisplayed.SolutionDisplayedBuilder<?, ?> solutionDisplayedBuilder =
        (SolutionDisplayed.SolutionDisplayedBuilder<?, ?>)
            fillInCommonBuilderFields(trainingRun, SolutionDisplayed.builder());

    SolutionDisplayed solutionDisplayed =
        solutionDisplayedBuilder
            .penaltyPoints(trainingRun.getMaxLevelScore() - trainingRun.getCurrentPenalty())
            .build();
    auditService.saveTrainingRunEvent(solutionDisplayed);
  }

  /**
   * Audit correct answer submitted action.
   *
   * @param trainingRun the training run
   * @param answer the answer
   */
  public void auditCorrectAnswerSubmittedAction(TrainingRun trainingRun, String answer) {
    CorrectAnswerSubmitted.CorrectAnswerSubmittedBuilder<?, ?> correctAnswerSubmittedBuilder =
        (CorrectAnswerSubmitted.CorrectAnswerSubmittedBuilder<?, ?>)
            fillInCommonBuilderFields(trainingRun, CorrectAnswerSubmitted.builder());

    CorrectAnswerSubmitted correctAnswerSubmitted =
        correctAnswerSubmittedBuilder.answerContent(answer).build();
    auditService.saveTrainingRunEvent(correctAnswerSubmitted);
  }

  /**
   * Audit wrong answer submitted action.
   *
   * @param trainingRun the training run
   * @param answer the answer
   */
  public void auditWrongAnswerSubmittedAction(TrainingRun trainingRun, String answer) {
    WrongAnswerSubmitted.WrongAnswerSubmittedBuilder<?, ?> wrongAnswerSubmittedBuilder =
        (WrongAnswerSubmitted.WrongAnswerSubmittedBuilder<?, ?>)
            fillInCommonBuilderFields(trainingRun, WrongAnswerSubmitted.builder());

    WrongAnswerSubmitted wrongAnswerSubmitted =
        wrongAnswerSubmittedBuilder
            .answerContent(answer)
            .count(trainingRun.getIncorrectAnswerCount())
            .build();
    auditService.saveTrainingRunEvent(wrongAnswerSubmitted);
  }

  /**
   * Audit assessment answers action.
   *
   * @param trainingRun the training run
   * @param answers the answers
   */
  public void auditAssessmentAnswersAction(TrainingRun trainingRun, String answers) {
    AssessmentAnswered.AssessmentAnsweredBuilder<?, ?> assessmentAnswersBuilder =
        (AssessmentAnswered.AssessmentAnsweredBuilder<?, ?>)
            fillInCommonBuilderFields(trainingRun, AssessmentAnswered.builder());

    AssessmentAnswered assessmentAnswers = assessmentAnswersBuilder.answers(answers).build();
    auditService.saveTrainingRunEvent(assessmentAnswers);
  }

  /**
   * Audit training run ended action.
   *
   * @param trainingRun the training run
   */
  public void auditTrainingRunEndedAction(TrainingRun trainingRun) {
    TrainingRunFinished.TrainingRunFinishedBuilder<?, ?> trainingRunEndedBuilder =
        (TrainingRunFinished.TrainingRunFinishedBuilder<?, ?>)
            fillInCommonBuilderFields(trainingRun, TrainingRunFinished.builder());

    TrainingRunFinished trainingRunFinished =
        trainingRunEndedBuilder
            .startTime(
                trainingRun.getStartTime().atOffset(ZoneOffset.UTC).toInstant().toEpochMilli())
            .endTime(System.currentTimeMillis())
            .build();
    auditService.saveTrainingRunEvent(trainingRunFinished, 3);
  }

  /**
   * Audit training run resumed action.
   *
   * @param trainingRun the training run
   */
  public void auditTrainingRunResumedAction(TrainingRun trainingRun) {
    TrainingRunResumed.TrainingRunResumedBuilder<?, ?> trainingRunResumedBuilder =
        (TrainingRunResumed.TrainingRunResumedBuilder<?, ?>)
            fillInCommonBuilderFields(trainingRun, TrainingRunResumed.builder());
    TrainingRunResumed trainingRunResumed = trainingRunResumedBuilder.build();
    auditService.saveTrainingRunEvent(trainingRunResumed);
  }

  private AbstractAuditPOJO.AbstractAuditPOJOBuilder<?, ?> fillInCommonBuilderFields(
      TrainingRun trainingRun, AbstractAuditPOJO.AbstractAuditPOJOBuilder<?, ?> builder) {
    TrainingInstance trainingInstance = trainingRun.getTrainingInstance();
    AbstractLevel trainingLevel = trainingRun.getCurrentLevel();
    builder
        .trainingRunId(trainingRun.getId())
        .poolId(trainingInstance.getPoolId())
        .sandboxId(trainingRun.getSandboxInstanceRefId())
        .trainingInstanceId(trainingInstance.getId())
        .trainingDefinitionId(trainingInstance.getTrainingDefinition().getId())
        .trainingTime(computeTrainingTime(trainingRun.getStartTime()))
        .userRefId(trainingRun.getParticipantRef().getUserRefId())
        .level(trainingLevel.getId())
        .levelOrder(trainingLevel.getOrder())
        .totalTrainingScore(trainingRun.getTotalTrainingScore())
        .totalAssessmentScore(trainingRun.getTotalAssessmentScore())
        .actualScoreInLevel(trainingRun.getMaxLevelScore() - trainingRun.getCurrentPenalty());
    return builder;
  }

  private long computeTrainingTime(LocalDateTime trainingStartedTime) {
    return ChronoUnit.MILLIS.between(trainingStartedTime, LocalDateTime.now(Clock.systemUTC()));
  }

  private EventLevelType getLevelType(AbstractLevel abstractLevel) {
    if (abstractLevel instanceof TrainingLevel) {
      return EventLevelType.TRAINING;
    } else if (abstractLevel instanceof InfoLevel) {
      return EventLevelType.INFO;
    } else if (abstractLevel instanceof AssessmentLevel) {
      return EventLevelType.ASSESSMENT;
    } else if (abstractLevel instanceof AccessLevel) {
      return EventLevelType.ACCESS;
    }
    return EventLevelType.PVP;
  }
}
