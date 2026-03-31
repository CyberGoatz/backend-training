package cz.cyberrange.platform.training.service.services.detection;

import static cz.cyberrange.platform.training.service.utils.CheatingDetectionUtils.*;

import cz.cyberrange.platform.training.api.responses.VariantAnswer;
import cz.cyberrange.platform.training.persistence.model.Submission;
import cz.cyberrange.platform.training.persistence.model.TrainingLevel;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import cz.cyberrange.platform.training.persistence.model.detection.AnswerSimilarityDetectionEvent;
import cz.cyberrange.platform.training.persistence.model.detection.CheatingDetection;
import cz.cyberrange.platform.training.persistence.model.detection.DetectionEventParticipant;
import cz.cyberrange.platform.training.persistence.model.enums.DetectionEventType;
import cz.cyberrange.platform.training.persistence.repository.SubmissionRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingLevelRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingRunRepository;
import cz.cyberrange.platform.training.persistence.repository.detection.AnswerSimilarityDetectionEventRepository;
import cz.cyberrange.platform.training.service.services.TrainingInstanceService;
import cz.cyberrange.platform.training.service.services.TrainingRunService;
import cz.cyberrange.platform.training.service.services.UserService;
import cz.cyberrange.platform.training.service.services.api.AnswersStorageApiService;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AnswerSimilarityService {
  private static final Logger LOG = LoggerFactory.getLogger(CheatingDetectionService.class);
  private final TrainingLevelRepository trainingLevelRepository;
  private final SubmissionRepository submissionRepository;
  private final AnswerSimilarityDetectionEventRepository answerSimilarityDetectionEventRepository;
  private final TrainingRunRepository trainingRunRepository;
  private final AnswersStorageApiService answersStorageApiService;
  private final TrainingRunService trainingRunService;
  private final TrainingInstanceService trainingInstanceService;
  private final UserService userService;
  private final DetectionEventService detectionEventService;

  /**
   * Instantiates a new Cheating detection service.
   *
   * @param trainingLevelRepository the training level repository
   * @param submissionRepository the submission repository
   * @param answerSimilarityDetectionEventRepository the answer similarity detection event
   *     repository
   * @param trainingRunRepository the training run repository
   * @param answersStorageApiService the answers storage api service
   * @param trainingRunService the training run service
   * @param trainingInstanceService the training instance service
   * @param userService the user service
   */
  @Autowired
  public AnswerSimilarityService(
      TrainingLevelRepository trainingLevelRepository,
      SubmissionRepository submissionRepository,
      AnswerSimilarityDetectionEventRepository answerSimilarityDetectionEventRepository,
      TrainingRunRepository trainingRunRepository,
      AnswersStorageApiService answersStorageApiService,
      TrainingRunService trainingRunService,
      TrainingInstanceService trainingInstanceService,
      UserService userService,
      DetectionEventService detectionEventService) {
    this.trainingLevelRepository = trainingLevelRepository;
    this.submissionRepository = submissionRepository;
    this.answerSimilarityDetectionEventRepository = answerSimilarityDetectionEventRepository;
    this.trainingRunRepository = trainingRunRepository;
    this.answersStorageApiService = answersStorageApiService;
    this.trainingRunService = trainingRunService;
    this.trainingInstanceService = trainingInstanceService;
    this.userService = userService;
    this.detectionEventService = detectionEventService;
  }

  /**
   * finds all answer similarity events of a cheating detection
   *
   * @param cheatingDetectionId the cheating detection id
   * @return list of events
   */
  public List<AnswerSimilarityDetectionEvent> findAllAnswerSimilarityEventsOfDetection(
      Long cheatingDetectionId) {
    return answerSimilarityDetectionEventRepository.findAllByCheatingDetectionId(
        cheatingDetectionId);
  }

  /**
   * Finds specific answer similarity event by it's ID
   *
   * @param eventId the event id
   * @return the event
   */
  public AnswerSimilarityDetectionEvent findAnswerSimilarityEventById(Long eventId) {
    return answerSimilarityDetectionEventRepository.findAnswerSimilarityEventById(eventId);
  }

  /**
   * Executes a cheating detection method of answer similarity
   *
   * @param cd the cheating detection
   */
  void executeCheatingDetectionOfAnswerSimilarity(CheatingDetection cd) {
    Long trainingInstanceId = cd.getTrainingInstanceId();
    Set<TrainingRun> runs = trainingRunService.findAllByTrainingInstanceId(trainingInstanceId);
    Map<String, List<VariantAnswer>> answers = new HashMap<>();
    Map<Long, TrainingLevel> trainingLevelsById =
        aggregateTrainingLevelsById(trainingInstanceId, runs, answers);
    for (Submission submission :
        submissionRepository.getIncorrectSubmissionsOfTrainingInstance(trainingInstanceId)) {
      evaluateAnswerSimilarityForSubmission(cd, runs, answers, trainingLevelsById, submission);
    }
  }

  private Map<Long, TrainingLevel> aggregateTrainingLevelsById(
      Long trainingInstanceId, Set<TrainingRun> runs, Map<String, List<VariantAnswer>> answers) {
    runs.forEach(
        run -> {
          String sandboxId = run.getSandboxInstanceRefId();
          answers.put(
              sandboxId,
              answersStorageApiService.getAnswersBySandboxId(sandboxId).getVariantAnswers());
        });

    Long trainingDefinitionId =
        trainingInstanceService.findById(trainingInstanceId).getTrainingDefinition().getId();
    return trainingLevelRepository.findAllByTrainingDefinitionId(trainingDefinitionId).stream()
        .collect(Collectors.toMap(TrainingLevel::getId, level -> level));
  }

  private void evaluateAnswerSimilarityForSubmission(
      CheatingDetection cd,
      Set<TrainingRun> runs,
      Map<String, List<VariantAnswer>> answers,
      Map<Long, TrainingLevel> trainingLevelsById,
      Submission submission) {
    Long currentId = submission.getLevel().getId();
    String sandboxId = submission.getTrainingRun().getSandboxInstanceRefId();

    if (checkIfAnswerBelongsToDifferentLevel(answers.get(sandboxId), submission.getProvided())
        || !trainingLevelsById.containsKey(currentId)) {
      return;
    }

    runs.stream()
        .map(run -> Map.entry(run, answers.get(run.getSandboxInstanceRefId())))
        .forEach(
            entry ->
                validateAndLogAnswerSimilarityEvent(
                    entry.getKey(),
                    submission,
                    entry.getValue(),
                    trainingLevelsById.get(currentId).getAnswerVariableName(),
                    cd));
  }

  private boolean checkIfAnswerBelongsToDifferentLevel(
      List<VariantAnswer> answers, String provided) {
    return answers.stream().map(VariantAnswer::getAnswerContent).anyMatch(provided::equals);
  }

  private void validateAndLogAnswerSimilarityEvent(
      TrainingRun run,
      Submission submission,
      List<VariantAnswer> answers,
      String answerVariable,
      CheatingDetection cd) {
    String submissionSandboxId = submission.getTrainingRun().getSandboxInstanceRefId();
    String sandboxId = run.getSandboxInstanceRefId();
    Set<DetectionEventParticipant> participants = new HashSet<>();
    if (sandboxId.equals(submissionSandboxId)) {
      return;
    }
    populateParticipants(submission, answers, answerVariable, participants);
    if (!participants.isEmpty()) {
      generateAnswerSimilarityEvent(run, submission, cd, participants);
    }
  }

  private void generateAnswerSimilarityEvent(
      TrainingRun run,
      Submission submission,
      CheatingDetection cd,
      Set<DetectionEventParticipant> participants) {
    String answerOwner =
        userService
            .getUserRefDTOByUserRefId(run.getParticipantRef().getUserRefId())
            .getUserRefFullName();
    List<Submission> ownerSubmissions =
        submissionRepository.getCorrectSubmissionsOfTrainingRunSorted(run.getId());
    for (var ownerSubmission : ownerSubmissions) {
      if (Objects.equals(ownerSubmission.getLevel().getId(), submission.getLevel().getId())) {
        participants.add(
            extractParticipant(
                ownerSubmission,
                userService
                    .getUserRefDTOByUserRefId(
                        ownerSubmission.getTrainingRun().getParticipantRef().getUserRefId())
                    .getUserRefFullName()));
      }
    }
    auditAnswerSimilarityEvent(submission, cd, participants, answerOwner);
    run.setHasDetectionEvent(true);
    trainingRunRepository.save(run);
  }

  private void populateParticipants(
      Submission submission,
      List<VariantAnswer> answers,
      String answerVariable,
      Set<DetectionEventParticipant> participants) {
    for (var answer : answers) {
      if (answer.getAnswerContent().equals(submission.getProvided())
          && answerVariable.equals(answer.getAnswerVariableName())) {
        DetectionEventParticipant participant =
            extractParticipant(
                submission,
                userService
                    .getUserRefDTOByUserRefId(
                        submission.getTrainingRun().getParticipantRef().getUserRefId())
                    .getUserRefFullName());
        if (!checkIfContainsParticipant(participants, participant)) {
          participants.add(participant);
        }
      }
    }
  }

  private void auditAnswerSimilarityEvent(
      Submission submission,
      CheatingDetection cd,
      Set<DetectionEventParticipant> participants,
      String answerOwner) {
    TrainingRun run = submission.getTrainingRun();
    run.setHasDetectionEvent(true);
    trainingRunRepository.save(run);
    AnswerSimilarityDetectionEvent event = new AnswerSimilarityDetectionEvent();
    event.setCommonDetectionEventParameters(
        submission, cd, DetectionEventType.ANSWER_SIMILARITY, participants.size());
    event.setAnswer(submission.getProvided());
    event.setAnswerOwner(answerOwner);
    event.setParticipants(generateParticipantString(participants));
    answerSimilarityDetectionEventRepository.save(event);
    detectionEventService.saveParticipants(participants, event.getId(), cd.getId());
  }
}
