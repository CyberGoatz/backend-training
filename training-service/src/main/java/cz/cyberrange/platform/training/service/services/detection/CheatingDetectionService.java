package cz.cyberrange.platform.training.service.services.detection;

import cz.cyberrange.platform.training.api.exceptions.EntityErrorDetail;
import cz.cyberrange.platform.training.api.exceptions.EntityNotFoundException;
import cz.cyberrange.platform.training.persistence.model.AbstractEntity;
import cz.cyberrange.platform.training.persistence.model.detection.AbstractDetectionEvent;
import cz.cyberrange.platform.training.persistence.model.detection.CheatingDetection;
import cz.cyberrange.platform.training.persistence.model.detection.DetectionEventParticipant;
import cz.cyberrange.platform.training.persistence.model.enums.CheatingDetectionState;
import cz.cyberrange.platform.training.persistence.repository.TrainingRunRepository;
import cz.cyberrange.platform.training.persistence.repository.detection.AbstractDetectionEventRepository;
import cz.cyberrange.platform.training.persistence.repository.detection.CheatingDetectionRepository;
import cz.cyberrange.platform.training.persistence.repository.detection.DetectedForbiddenCommandRepository;
import cz.cyberrange.platform.training.persistence.repository.detection.DetectionEventParticipantRepository;
import cz.cyberrange.platform.training.service.services.TrainingRunService;
import cz.cyberrange.platform.training.service.services.UserService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/** The type Cheating detection service. */
@Service
public class CheatingDetectionService {

  private static final Logger LOG = LoggerFactory.getLogger(CheatingDetectionService.class);
  private final AbstractDetectionEventRepository detectionEventRepository;
  private final CheatingDetectionRepository cheatingDetectionRepository;
  private final DetectionEventParticipantRepository detectionEventParticipantRepository;
  private final DetectedForbiddenCommandRepository detectedForbiddenCommandRepository;
  private final TrainingRunRepository trainingRunRepository;
  private final TrainingRunService trainingRunService;
  private final UserService userService;
  private final AnswerSimilarityService answerSimilarityService;
  private final LocationSimilarityService locationSimilarityService;
  private final MinimalSolveTimeService minimalSolveTimeService;
  private final TimeProximityService timeProximityService;
  private final NoCommandsService noCommandsService;
  private final ForbiddenCommandsService forbiddenCommandsService;

  /**
   * Instantiates a new Cheating detection service.
   *
   * @param abstractDetectionEventRepository the cheat repository
   * @param cheatingDetectionRepository the cheating detection repository
   * @param detectionEventParticipantRepository the detection event participant repository
   * @param detectedForbiddenCommandRepository the detected forbidden commands repository
   * @param trainingRunRepository the training run repository
   * @param trainingRunService the training run service
   * @param userService the user service
   * @param answerSimilarityService the answer similarity service
   * @param locationSimilarityService the location similarity service
   * @param minimalSolveTimeService the minimal solve time service
   * @param timeProximityService the time proximity service
   * @param noCommandsService the no commands service
   * @param forbiddenCommandsService the forbidden commands service
   */
  @Autowired
  public CheatingDetectionService(
      AbstractDetectionEventRepository abstractDetectionEventRepository,
      CheatingDetectionRepository cheatingDetectionRepository,
      DetectionEventParticipantRepository detectionEventParticipantRepository,
      DetectedForbiddenCommandRepository detectedForbiddenCommandRepository,
      TrainingRunRepository trainingRunRepository,
      TrainingRunService trainingRunService,
      UserService userService,
      AnswerSimilarityService answerSimilarityService,
      LocationSimilarityService locationSimilarityService,
      MinimalSolveTimeService minimalSolveTimeService,
      TimeProximityService timeProximityService,
      NoCommandsService noCommandsService,
      ForbiddenCommandsService forbiddenCommandsService) {
    this.detectionEventRepository = abstractDetectionEventRepository;
    this.cheatingDetectionRepository = cheatingDetectionRepository;
    this.detectionEventParticipantRepository = detectionEventParticipantRepository;
    this.detectedForbiddenCommandRepository = detectedForbiddenCommandRepository;
    this.trainingRunRepository = trainingRunRepository;
    this.trainingRunService = trainingRunService;
    this.userService = userService;
    this.answerSimilarityService = answerSimilarityService;
    this.locationSimilarityService = locationSimilarityService;
    this.minimalSolveTimeService = minimalSolveTimeService;
    this.timeProximityService = timeProximityService;
    this.noCommandsService = noCommandsService;
    this.forbiddenCommandsService = forbiddenCommandsService;
  }

  /**
   * Creates a new cheating detection
   *
   * @param cheatingDetection to be created
   */
  public void createCheatingDetection(CheatingDetection cheatingDetection) {
    cheatingDetection.setExecutedBy(userService.getUserRefFromUserAndGroup().getUserRefFullName());
    cheatingDetection.setResults(0L);
    cheatingDetection.setExecuteTime(LocalDateTime.now());
    cheatingDetectionRepository.save(cheatingDetection);
  }

  /**
   * Executes a cheating detection
   *
   * @param cd the cheating detection to be executed
   */
  public void executeCheatingDetection(CheatingDetection cd) {
    cd.setCurrentState(CheatingDetectionState.RUNNING);
    executeSelectedCheatingDetectionMethods(cd);
    cd.setCurrentState(CheatingDetectionState.FINISHED);
    updateCheatingDetection(cd);
  }

  /**
   * deletes cheating detection
   *
   * @param cheatingDetectionId the id of the cheating detection
   * @param trainingInstanceId the id training instance
   */
  public void deleteCheatingDetection(Long cheatingDetectionId, Long trainingInstanceId) {
    trainingRunService.findAllByTrainingInstanceId(trainingInstanceId).stream()
        .peek(run -> run.setHasDetectionEvent(false))
        .forEach(trainingRunRepository::save);
    List<AbstractDetectionEvent> events =
        detectionEventRepository.findAllByCheatingDetectionId(cheatingDetectionId);
    detectionEventParticipantRepository.deleteAllParticipantsByCheatingDetectionId(
        cheatingDetectionId);
    events.stream()
        .map(AbstractDetectionEvent::getId)
        .forEach(detectedForbiddenCommandRepository::deleteAllByDetectionEventId);
    detectionEventRepository.deleteDetectionEventsOfCheatingDetection(cheatingDetectionId);
    cheatingDetectionRepository.deleteCheatingDetectionById(cheatingDetectionId);
  }

  /**
   * deletes all cheating detection of training instance
   *
   * @param trainingInstanceId the training instance id
   */
  public void deleteAllCheatingDetectionsOfTrainingInstance(Long trainingInstanceId) {
    cheatingDetectionRepository.findAllByTrainingInstanceId(trainingInstanceId).stream()
        .map(CheatingDetection::getId)
        .forEach(detectionId -> deleteCheatingDetection(detectionId, trainingInstanceId));
  }

  /**
   * finds cheating detection by provided id
   *
   * @param cheatingDetectionId the cheating detection id
   * @return cheating detection
   */
  public CheatingDetection findCheatingDetectionById(Long cheatingDetectionId) {
    return cheatingDetectionRepository.findCheatingDetectionById(cheatingDetectionId);
  }

  /**
   * re-executes an existing cheating detection
   *
   * @param cheatingDetectionId id of the cheating detection
   */
  public void reExecuteCheatingDetection(Long cheatingDetectionId) {
    CheatingDetection cd =
        Optional.ofNullable(
                cheatingDetectionRepository.findCheatingDetectionById(cheatingDetectionId))
            .orElseThrow(
                () ->
                    new EntityNotFoundException(
                        new EntityErrorDetail(
                            CheatingDetection.class,
                            "id",
                            cheatingDetectionId.getClass(),
                            cheatingDetectionId)));

    cd.setExecuteStates();

    detectionEventRepository.findAllByCheatingDetectionId(cheatingDetectionId).stream()
        .map(AbstractEntity::getId)
        .forEach(detectedForbiddenCommandRepository::deleteAllByDetectionEventId);

    detectionEventRepository.deleteDetectionEventsOfCheatingDetection(cheatingDetectionId);
    detectionEventParticipantRepository.deleteAllParticipantsByCheatingDetectionId(
        cheatingDetectionId);

    cd.setExecuteTime(LocalDateTime.now());
    cd.setResults(0L);
    cheatingDetectionRepository.save(cd);
    executeCheatingDetection(cd);
  }

  /**
   * finds all detection event participants of a cheating detection
   *
   * @param cheatingDetectionId the cheating detection id
   * @return detection event participants
   */
  public List<DetectionEventParticipant> findAllParticipantsOfCheatingDetection(
      Long cheatingDetectionId) {
    return detectionEventParticipantRepository.findAllParticipantsOfCheatingDetection(
        cheatingDetectionId);
  }

  /**
   * finds all cheating detection of a training instance
   *
   * @param trainingInstanceId the training instance id
   * @param pageable the pageable
   * @return page of cheating detection
   */
  public Page<CheatingDetection> findAllCheatingDetectionsOfTrainingInstance(
      Long trainingInstanceId, Pageable pageable) {
    return cheatingDetectionRepository.findAllByTrainingInstanceId(trainingInstanceId, pageable);
  }

  private void updateCheatingDetection(CheatingDetection cd) {
    cheatingDetectionRepository.save(cd);
  }

  private void executeSelectedCheatingDetectionMethods(CheatingDetection cd) {
    updateCheatingDetection(cd);
    handleAnswerSimilarityExecution(cd);
    handleLocationSimilarityExecution(cd);
    handleTimeProximityExecution(cd);
    handleMinimalSolveTimeExecution(cd);
    handleNoCommandsExecution(cd);
    handleForbiddenCommandsExecution(cd);
    cd.setResults(detectionEventRepository.getNumberOfDetections(cd.getId()));
  }

  private void handleForbiddenCommandsExecution(CheatingDetection cd) {
    if (cd.getForbiddenCommandsState() == CheatingDetectionState.QUEUED) {
      cd.setForbiddenCommandsState(CheatingDetectionState.RUNNING);
      updateCheatingDetection(cd);
      forbiddenCommandsService.executeCheatingDetectionOfForbiddenCommands(cd);
      cd.setForbiddenCommandsState(CheatingDetectionState.FINISHED);
      updateCheatingDetection(cd);
    }
  }

  private void handleNoCommandsExecution(CheatingDetection cd) {
    if (cd.getNoCommandsState() == CheatingDetectionState.QUEUED) {
      cd.setNoCommandsState(CheatingDetectionState.RUNNING);
      updateCheatingDetection(cd);
      noCommandsService.executeCheatingDetectionOfNoCommands(cd);
      cd.setNoCommandsState(CheatingDetectionState.FINISHED);
      updateCheatingDetection(cd);
    }
  }

  private void handleMinimalSolveTimeExecution(CheatingDetection cd) {
    if (cd.getMinimalSolveTimeState() == CheatingDetectionState.QUEUED) {
      cd.setMinimalSolveTimeState(CheatingDetectionState.RUNNING);
      updateCheatingDetection(cd);
      minimalSolveTimeService.executeCheatingDetectionOfMinimalSolveTime(cd);
      cd.setMinimalSolveTimeState(CheatingDetectionState.FINISHED);
      updateCheatingDetection(cd);
    }
  }

  private void handleTimeProximityExecution(CheatingDetection cd) {
    if (cd.getTimeProximityState() == CheatingDetectionState.QUEUED) {
      if (cd.getProximityThreshold() == null) {
        cd.setProximityThreshold(120L);
      }
      cd.setTimeProximityState(CheatingDetectionState.RUNNING);
      updateCheatingDetection(cd);
      timeProximityService.executeCheatingDetectionOfTimeProximity(cd);
      cd.setTimeProximityState(CheatingDetectionState.FINISHED);
      updateCheatingDetection(cd);
    }
  }

  private void handleLocationSimilarityExecution(CheatingDetection cd) {
    if (cd.getLocationSimilarityState() == CheatingDetectionState.QUEUED) {
      cd.setLocationSimilarityState(CheatingDetectionState.RUNNING);
      updateCheatingDetection(cd);
      locationSimilarityService.executeCheatingDetectionOfLocationSimilarity(cd);
      cd.setLocationSimilarityState(CheatingDetectionState.FINISHED);
      updateCheatingDetection(cd);
    }
  }

  private void handleAnswerSimilarityExecution(CheatingDetection cd) {
    if (cd.getAnswerSimilarityState() == CheatingDetectionState.QUEUED) {
      cd.setAnswerSimilarityState(CheatingDetectionState.RUNNING);
      updateCheatingDetection(cd);
      answerSimilarityService.executeCheatingDetectionOfAnswerSimilarity(cd);
      cd.setAnswerSimilarityState(CheatingDetectionState.FINISHED);
      updateCheatingDetection(cd);
    }
  }
}
