package cz.cyberrange.platform.training.service.services.detection;

import com.querydsl.core.types.Predicate;
import cz.cyberrange.platform.training.persistence.model.Submission;
import cz.cyberrange.platform.training.persistence.model.detection.AbstractDetectionEvent;
import cz.cyberrange.platform.training.persistence.model.detection.DetectedForbiddenCommand;
import cz.cyberrange.platform.training.persistence.model.detection.DetectionEventParticipant;
import cz.cyberrange.platform.training.persistence.repository.detection.AbstractDetectionEventRepository;
import cz.cyberrange.platform.training.persistence.repository.detection.DetectedForbiddenCommandRepository;
import cz.cyberrange.platform.training.persistence.repository.detection.DetectionEventParticipantRepository;
import cz.cyberrange.platform.training.service.services.UserService;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class DetectionEventService {
  private static final Logger LOG = LoggerFactory.getLogger(CheatingDetectionService.class);
  private final AbstractDetectionEventRepository detectionEventRepository;
  private final DetectionEventParticipantRepository detectionEventParticipantRepository;
  private final DetectedForbiddenCommandRepository detectedForbiddenCommandRepository;
  private final UserService userService;

  /**
   * Instantiates a new Cheating detection service.
   *
   * @param abstractDetectionEventRepository the cheat repository
   * @param detectionEventParticipantRepository the detection event participant repository
   * @param detectedForbiddenCommandRepository the detected forbidden commands repository
   * @param userService the user service
   */
  @Autowired
  public DetectionEventService(
      AbstractDetectionEventRepository abstractDetectionEventRepository,
      DetectionEventParticipantRepository detectionEventParticipantRepository,
      DetectedForbiddenCommandRepository detectedForbiddenCommandRepository,
      UserService userService) {
    this.detectionEventRepository = abstractDetectionEventRepository;
    this.detectionEventParticipantRepository = detectionEventParticipantRepository;
    this.detectedForbiddenCommandRepository = detectedForbiddenCommandRepository;
    this.userService = userService;
  }

  /**
   * deletes all detection events of a given cheating detection
   *
   * @param cheatingDetectionId the cheating detection id
   */
  public void deleteDetectionEvents(Long cheatingDetectionId) {
    detectionEventRepository.deleteDetectionEventsOfCheatingDetection(cheatingDetectionId);
  }

  /**
   * finds all events of a cheating detection
   *
   * @param cheatingDetectionId the cheating detection id
   * @param pageable the pageable
   * @param predicate the predicate
   * @return page of detection events
   */
  public Page<AbstractDetectionEvent> findAllDetectionEventsOfCheatingDetection(
      Long cheatingDetectionId, Pageable pageable, Predicate predicate) {
    return detectionEventRepository.findAllByCheatingDetectionId(
        cheatingDetectionId, pageable, predicate);
  }

  /**
   * finds all participants of a detection event
   *
   * @param eventId the event id
   * @return detection event participants
   */
  public List<DetectionEventParticipant> findAllParticipantsOfEvent(Long eventId) {
    return detectionEventParticipantRepository.findAllByEventId(eventId);
  }

  /**
   * finds all forbidden commands of a detection event
   *
   * @param eventId the event id
   * @param pageable the pageable
   * @return page of detected forbidden commands
   */
  public Page<DetectedForbiddenCommand> findAllForbiddenCommandsOfDetectionEvent(
      Long eventId, Pageable pageable) {
    return detectedForbiddenCommandRepository.findAllByEventId(eventId, pageable);
  }

  /**
   * finds all forbidden commands of a detection event
   *
   * @param eventId the event id
   * @return page of detected forbidden commands
   */
  public List<DetectedForbiddenCommand> findAllForbiddenCommandsOfDetectionEvent(Long eventId) {
    return detectedForbiddenCommandRepository.findAllByEventId(eventId);
  }

  public AbstractDetectionEvent findDetectionEventById(Long eventId) {
    return detectionEventRepository.findDetectionEventById(eventId);
  }

  void saveParticipants(
      Set<DetectionEventParticipant> participants, Long eventId, Long cheatingDetectionId) {
    for (var participant : participants) {
      participant.setDetectionEventId(eventId);
      participant.setCheatingDetectionId(cheatingDetectionId);
      detectionEventParticipantRepository.save(participant);
    }
  }

  public Page<DetectionEventParticipant> findAllParticipantsOfEvent(
      Long eventId, Pageable pageable) {
    return detectionEventParticipantRepository.findAllByEventId(eventId, pageable);
  }

  String getUserFullName(Submission currentSubmission) {
    return userService
        .getUserRefDTOByUserRefId(
            currentSubmission.getTrainingRun().getParticipantRef().getUserRefId())
        .getUserRefFullName();
  }
}
