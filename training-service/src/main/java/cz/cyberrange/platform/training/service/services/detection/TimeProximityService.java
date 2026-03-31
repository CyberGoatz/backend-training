package cz.cyberrange.platform.training.service.services.detection;

import static cz.cyberrange.platform.training.service.utils.CheatingDetectionUtils.extractParticipant;
import static cz.cyberrange.platform.training.service.utils.CheatingDetectionUtils.generateParticipantString;

import cz.cyberrange.platform.training.persistence.model.Submission;
import cz.cyberrange.platform.training.persistence.model.TrainingLevel;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import cz.cyberrange.platform.training.persistence.model.detection.CheatingDetection;
import cz.cyberrange.platform.training.persistence.model.detection.DetectionEventParticipant;
import cz.cyberrange.platform.training.persistence.model.detection.TimeProximityDetectionEvent;
import cz.cyberrange.platform.training.persistence.model.enums.DetectionEventType;
import cz.cyberrange.platform.training.persistence.repository.SubmissionRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingLevelRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingRunRepository;
import cz.cyberrange.platform.training.persistence.repository.detection.TimeProximityDetectionEventRepository;
import cz.cyberrange.platform.training.service.services.TrainingInstanceService;
import cz.cyberrange.platform.training.service.services.TrainingRunService;
import cz.cyberrange.platform.training.service.utils.CheatingDetectionUtils;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TimeProximityService {
  private static final Logger LOG = LoggerFactory.getLogger(CheatingDetectionService.class);
  private final TrainingLevelRepository trainingLevelRepository;
  private final SubmissionRepository submissionRepository;
  private final TimeProximityDetectionEventRepository timeProximityDetectionEventRepository;
  private final TrainingRunRepository trainingRunRepository;
  private final TrainingRunService trainingRunService;
  private final TrainingInstanceService trainingInstanceService;
  private final DetectionEventService detectionEventService;

  /**
   * Instantiates a new Cheating detection service.
   *
   * @param trainingLevelRepository the training level repository
   * @param submissionRepository the submission repository
   * @param timeProximityDetectionEventRepository the time proximity detection event repository
   * @param trainingRunRepository the training run repository
   * @param trainingRunService the training run service
   * @param trainingInstanceService the training instance service
   */
  @Autowired
  public TimeProximityService(
      TrainingLevelRepository trainingLevelRepository,
      SubmissionRepository submissionRepository,
      TimeProximityDetectionEventRepository timeProximityDetectionEventRepository,
      TrainingRunRepository trainingRunRepository,
      TrainingRunService trainingRunService,
      TrainingInstanceService trainingInstanceService,
      DetectionEventService detectionEventService) {
    this.trainingLevelRepository = trainingLevelRepository;
    this.submissionRepository = submissionRepository;
    this.timeProximityDetectionEventRepository = timeProximityDetectionEventRepository;
    this.trainingRunRepository = trainingRunRepository;
    this.trainingRunService = trainingRunService;
    this.trainingInstanceService = trainingInstanceService;
    this.detectionEventService = detectionEventService;
  }

  /**
   * finds all time proximity events of cheating deteciotn
   *
   * @param cheatingDetectionId the cheating detection id
   * @return events
   */
  public List<TimeProximityDetectionEvent> findAllTimeProximityEventsOfDetection(
      Long cheatingDetectionId) {
    return timeProximityDetectionEventRepository.findAllByCheatingDetectionId(cheatingDetectionId);
  }

  /**
   * finds time proximity event by ds
   *
   * @param eventId the event id
   * @return event
   */
  public TimeProximityDetectionEvent findTimeProximityEventById(Long eventId) {
    return timeProximityDetectionEventRepository.findTimeProximityEventById(eventId);
  }

  /**
   * Executes a cheating detection of type TIME_PROXIMITY
   *
   * @param cd the training instance id
   */
  void executeCheatingDetectionOfTimeProximity(CheatingDetection cd) {
    List<Submission> detectedGroup = new ArrayList<>();
    Set<DetectionEventParticipant> participants = new HashSet<>();

    for (var level :
        trainingLevelRepository.findAllByTrainingDefinitionId(
            trainingInstanceService
                .findById(cd.getTrainingInstanceId())
                .getTrainingDefinition()
                .getId())) {
      generateSuspiciousGroup(cd, detectedGroup, participants, level);
      for (var submission : detectedGroup) {
        generateEventParticipants(participants, submission);
      }
      if (!detectedGroup.isEmpty() && participants.size() > 1) {
        auditTimeProximityEvent(detectedGroup.get(0), cd, participants);
        detectedGroup.clear();
        participants.clear();
      }
    }
  }

  private void generateEventParticipants(
      Set<DetectionEventParticipant> participants, Submission submission) {
    DetectionEventParticipant participant =
        extractParticipant(submission, detectionEventService.getUserFullName(submission));
    if (!CheatingDetectionUtils.checkIfContainsParticipant(participants, participant)) {
      participants.add(participant);
    }
    trainingRunService.auditRunHasDetectionEvent(submission.getTrainingRun());
  }

  private void generateSuspiciousGroup(
      CheatingDetection cd,
      List<Submission> detectedGroup,
      Set<DetectionEventParticipant> participants,
      TrainingLevel level) {
    List<Submission> submissions;
    submissions =
        submissionRepository.getAllTimeProximitySubmissionsOfLevel(
            cd.getTrainingInstanceId(), level.getId());
    for (int submissionIndex = 1; submissionIndex < submissions.size(); submissionIndex++) {
      int previousIndex = submissionIndex - 1;
      var first = submissions.get(previousIndex);
      var second = submissions.get(submissionIndex);
      long timeProximity = Duration.between(first.getDate(), second.getDate()).toSeconds();
      if (timeProximity < cd.getProximityThreshold()) {
        if (detectedGroup.isEmpty()) {
          detectedGroup.add(first);
        }
        detectedGroup.add(second);
      } else {
        for (var submission : detectedGroup)
          participants.add(
              extractParticipant(submission, detectionEventService.getUserFullName(submission)));
        if (!detectedGroup.isEmpty()) {
          auditTimeProximityEvent(detectedGroup.get(0), cd, participants);
          detectedGroup.clear();
          participants.clear();
        }
      }
    }
  }

  private void auditTimeProximityEvent(
      Submission submission, CheatingDetection cd, Set<DetectionEventParticipant> participants) {
    TrainingRun run = submission.getTrainingRun();
    run.setHasDetectionEvent(true);
    trainingRunRepository.save(run);
    TimeProximityDetectionEvent event = new TimeProximityDetectionEvent();
    event.setCommonDetectionEventParameters(
        submission, cd, DetectionEventType.TIME_PROXIMITY, participants.size());
    event.setThreshold(cd.getProximityThreshold());
    event.setParticipants(generateParticipantString(participants));
    detectionEventService.saveParticipants(
        participants, timeProximityDetectionEventRepository.save(event).getId(), cd.getId());
  }
}
