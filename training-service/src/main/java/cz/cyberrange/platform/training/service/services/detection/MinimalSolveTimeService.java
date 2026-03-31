package cz.cyberrange.platform.training.service.services.detection;

import static cz.cyberrange.platform.training.service.utils.CheatingDetectionUtils.extractParticipant;
import static cz.cyberrange.platform.training.service.utils.CheatingDetectionUtils.generateParticipantString;

import cz.cyberrange.platform.training.persistence.model.Submission;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import cz.cyberrange.platform.training.persistence.model.detection.CheatingDetection;
import cz.cyberrange.platform.training.persistence.model.detection.DetectionEventParticipant;
import cz.cyberrange.platform.training.persistence.model.detection.MinimalSolveTimeDetectionEvent;
import cz.cyberrange.platform.training.persistence.model.enums.DetectionEventType;
import cz.cyberrange.platform.training.persistence.repository.SubmissionRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingRunRepository;
import cz.cyberrange.platform.training.persistence.repository.detection.MinimalSolveTimeDetectionEventRepository;
import cz.cyberrange.platform.training.service.services.TrainingRunService;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MinimalSolveTimeService {
  private static final Logger LOG = LoggerFactory.getLogger(CheatingDetectionService.class);
  private final SubmissionRepository submissionRepository;
  private final MinimalSolveTimeDetectionEventRepository minimalSolveTimeDetectionEventRepository;
  private final TrainingRunRepository trainingRunRepository;
  private final TrainingRunService trainingRunService;
  private final DetectionEventService detectionEventService;

  /**
   * Instantiates a new Cheating detection service.
   *
   * @param submissionRepository the submission repository
   * @param minimalSolveTimeDetectionEventRepository the minimal solve time detection event
   *     repository
   * @param trainingRunRepository the training run repository
   * @param trainingRunService the training run service
   */
  @Autowired
  public MinimalSolveTimeService(
      SubmissionRepository submissionRepository,
      MinimalSolveTimeDetectionEventRepository minimalSolveTimeDetectionEventRepository,
      TrainingRunRepository trainingRunRepository,
      TrainingRunService trainingRunService,
      DetectionEventService detectionEventService) {
    this.submissionRepository = submissionRepository;
    this.minimalSolveTimeDetectionEventRepository = minimalSolveTimeDetectionEventRepository;
    this.trainingRunRepository = trainingRunRepository;
    this.trainingRunService = trainingRunService;
    this.detectionEventService = detectionEventService;
  }

  /**
   * find all minimal solve time events of detection
   *
   * @param cheatingDetectionId the cheating detection id
   * @return list of events
   */
  public List<MinimalSolveTimeDetectionEvent> findAllMinimalSolveTimeEventsOfDetection(
      Long cheatingDetectionId) {
    return minimalSolveTimeDetectionEventRepository.findAllByCheatingDetectionId(
        cheatingDetectionId);
  }

  /**
   * find minimal solve time event by id
   *
   * @param eventId the event id
   * @return event
   */
  public MinimalSolveTimeDetectionEvent findMinimalSolveTimeEventById(Long eventId) {
    return minimalSolveTimeDetectionEventRepository.findMinimalSolveTimeEventById(eventId);
  }

  /**
   * find all minimal solve time events of participants for export
   *
   * @param cheatingDetectionId the cheating detection id
   * @param participants list of participant ids
   * @return list of events
   */
  List<MinimalSolveTimeDetectionEvent> findAllMinimalSolveTimeEventsOfGroup(
      Long cheatingDetectionId, List<Long> participants) {
    List<MinimalSolveTimeDetectionEvent> minimalSolveTimeEvents =
        findAllMinimalSolveTimeEventsOfDetection(cheatingDetectionId);
    List<MinimalSolveTimeDetectionEvent> result = new ArrayList<>();

    for (var event : minimalSolveTimeEvents) {
      if (!Collections.disjoint(
          detectionEventService.findAllParticipantsOfEvent(event.getId()).stream()
              .map(DetectionEventParticipant::getUserId)
              .toList(),
          participants)) {
        result.add(event);
      }
    }
    return result;
  }

  /**
   * Executes a cheating detection of type MINIMAL_SOLVE_TIME
   *
   * @param cd the training instance id
   */
  void executeCheatingDetectionOfMinimalSolveTime(CheatingDetection cd) {
    Map<Long, List<Submission>> suspiciousSubmissionsByLevel = new HashMap<>();
    Map<Long, Long> submissionTimes = new HashMap<>();
    aggregateMinimalSolveTimeSubmissionsByLevels(cd, suspiciousSubmissionsByLevel, submissionTimes);
    generateMinimalSolveTimeEvents(cd, suspiciousSubmissionsByLevel, submissionTimes);
  }

  private void auditMinimalSolveTimeEvent(
      Submission submission,
      CheatingDetection cd,
      Set<DetectionEventParticipant> participants,
      Long minimalSolveTime) {
    TrainingRun run = submission.getTrainingRun();
    run.setHasDetectionEvent(true);
    trainingRunRepository.save(run);
    MinimalSolveTimeDetectionEvent event = new MinimalSolveTimeDetectionEvent();
    event.setCommonDetectionEventParameters(
        submission, cd, DetectionEventType.MINIMAL_SOLVE_TIME, participants.size());
    event.setMinimalSolveTime(minimalSolveTime);
    event.setParticipants(generateParticipantString(participants));
    detectionEventService.saveParticipants(
        participants, minimalSolveTimeDetectionEventRepository.save(event).getId(), cd.getId());
  }

  private void aggregateMinimalSolveTimeSubmissionsByLevels(
      CheatingDetection cd,
      Map<Long, List<Submission>> detectedByLevel,
      Map<Long, Long> submissionTimes) {
    boolean isNewParticipant = true;
    LocalDateTime levelStart;
    Submission current;
    Submission previous = new Submission();
    for (Submission submission :
        submissionRepository.getCorrectSubmissionsOfTrainingInstance(cd.getTrainingInstanceId())) {
      current = submission;
      if (current.getLevel().getMinimalPossibleSolveTime() != null) {
        if (isNewParticipant) {
          levelStart = current.getTrainingRun().getStartTime();
          isNewParticipant = false;
        } else {
          if (current.getTrainingRun().equals(previous.getTrainingRun())) {
            levelStart = previous.getDate();
          } else {
            levelStart = current.getTrainingRun().getStartTime();
          }
        }
        long levelDuration = Duration.between(levelStart, current.getDate()).toSeconds();
        if (levelDuration < current.getLevel().getMinimalPossibleSolveTime() * 60) {
          addMinimalSolveTimeDataToMaps(detectedByLevel, submissionTimes, current, levelDuration);
        }
      }
      previous = current;
    }
  }

  private static void addMinimalSolveTimeDataToMaps(
      Map<Long, List<Submission>> detectedByLevel,
      Map<Long, Long> submissionTimes,
      Submission current,
      long levelDuration) {
    var submissions = detectedByLevel.get(current.getLevel().getId());
    if (submissions == null) {
      submissions = new ArrayList<>();
    }
    submissions.add(current);
    detectedByLevel.put(current.getLevel().getId(), submissions);
    submissionTimes.put(current.getId(), levelDuration);
  }

  private void generateMinimalSolveTimeEvents(
      CheatingDetection cd,
      Map<Long, List<Submission>> suspiciousSubmissionsByLevel,
      Map<Long, Long> submissionTimes) {
    Set<DetectionEventParticipant> participants;
    for (var submissions : suspiciousSubmissionsByLevel.entrySet()) {
      participants = new HashSet<>();
      for (var submission : submissions.getValue()) {
        trainingRunService.auditRunHasDetectionEvent(submission.getTrainingRun());
        participants.add(
            extractParticipant(
                submission,
                true,
                submissionTimes.get(submission.getId()),
                detectionEventService.getUserFullName(submission)));
      }
      Submission s = submissions.getValue().get(0);
      auditMinimalSolveTimeEvent(
          s, cd, participants, s.getLevel().getMinimalPossibleSolveTime() * 60);
    }
  }
}
