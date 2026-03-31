package cz.cyberrange.platform.training.service.facade.detection;

import com.querydsl.core.types.Predicate;
import cz.cyberrange.platform.training.api.dto.cheatingdetection.AbstractDetectionEventDTO;
import cz.cyberrange.platform.training.api.dto.cheatingdetection.AnswerSimilarityDetectionEventDTO;
import cz.cyberrange.platform.training.api.dto.cheatingdetection.DetectedForbiddenCommandDTO;
import cz.cyberrange.platform.training.api.dto.cheatingdetection.DetectionEventParticipantDTO;
import cz.cyberrange.platform.training.api.dto.cheatingdetection.ForbiddenCommandsDetectionEventDTO;
import cz.cyberrange.platform.training.api.dto.cheatingdetection.LocationSimilarityDetectionEventDTO;
import cz.cyberrange.platform.training.api.dto.cheatingdetection.MinimalSolveTimeDetectionEventDTO;
import cz.cyberrange.platform.training.api.dto.cheatingdetection.NoCommandsDetectionEventDTO;
import cz.cyberrange.platform.training.api.dto.cheatingdetection.TimeProximityDetectionEventDTO;
import cz.cyberrange.platform.training.api.responses.PageResultResource;
import cz.cyberrange.platform.training.service.annotations.transactions.TransactionalWO;
import cz.cyberrange.platform.training.service.mapping.mapstruct.detection.DetectedForbiddenCommandMapper;
import cz.cyberrange.platform.training.service.mapping.mapstruct.detection.DetectionEventMapper;
import cz.cyberrange.platform.training.service.mapping.mapstruct.detection.DetectionEventParticipantMapper;
import cz.cyberrange.platform.training.service.services.SecurityService;
import cz.cyberrange.platform.training.service.services.UserService;
import cz.cyberrange.platform.training.service.services.detection.AnswerSimilarityService;
import cz.cyberrange.platform.training.service.services.detection.DetectionEventService;
import cz.cyberrange.platform.training.service.services.detection.ForbiddenCommandsService;
import cz.cyberrange.platform.training.service.services.detection.LocationSimilarityService;
import cz.cyberrange.platform.training.service.services.detection.MinimalSolveTimeService;
import cz.cyberrange.platform.training.service.services.detection.NoCommandsService;
import cz.cyberrange.platform.training.service.services.detection.TimeProximityService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DetectionEventFacade {
  private final AnswerSimilarityService answerSimilarityService;
  private final LocationSimilarityService locationSimilarityService;
  private final MinimalSolveTimeService minimalSolveTimeService;
  private final TimeProximityService timeProximityService;
  private final NoCommandsService noCommandsService;
  private final ForbiddenCommandsService forbiddenCommandsService;
  private final DetectionEventService detectionEventService;
  public final UserService userService;
  private final DetectionEventMapper detectionEventMapper;
  private final DetectionEventParticipantMapper detectionEventParticipantMapper;

  private final DetectedForbiddenCommandMapper detectedForbiddenCommandMapper;
  private final SecurityService securityService;

  /**
   * Instantiates a new Cheating detection facade.
   *
   * @param userService the user service
   * @param detectionEventMapper the cheating detection mapper
   * @param detectionEventParticipantMapper the detection event participant mapper
   * @param forbiddenCommandMapper the forbidden command mapper
   * @param answerSimilarityService the answer similarity service
   * @param locationSimilarityService the location similarity service
   * @param minimalSolveTimeService the minimal solve time service
   * @param timeProximityService the time proximity service
   * @param noCommandsService the no commands service
   * @param forbiddenCommandsService the forbidden commands service
   * @param detectionEventService the detection event service
   * @param securityService the security service
   */
  @Autowired
  public DetectionEventFacade(
      UserService userService,
      DetectionEventMapper detectionEventMapper,
      DetectionEventParticipantMapper detectionEventParticipantMapper,
      DetectedForbiddenCommandMapper forbiddenCommandMapper,
      DetectionEventService detectionEventService,
      AnswerSimilarityService answerSimilarityService,
      LocationSimilarityService locationSimilarityService,
      MinimalSolveTimeService minimalSolveTimeService,
      TimeProximityService timeProximityService,
      NoCommandsService noCommandsService,
      ForbiddenCommandsService forbiddenCommandsService,
      SecurityService securityService) {
    this.userService = userService;
    this.detectionEventMapper = detectionEventMapper;
    this.detectionEventParticipantMapper = detectionEventParticipantMapper;
    this.detectedForbiddenCommandMapper = forbiddenCommandMapper;
    this.detectionEventService = detectionEventService;
    this.answerSimilarityService = answerSimilarityService;
    this.locationSimilarityService = locationSimilarityService;
    this.minimalSolveTimeService = minimalSolveTimeService;
    this.timeProximityService = timeProximityService;
    this.noCommandsService = noCommandsService;
    this.forbiddenCommandsService = forbiddenCommandsService;
    this.securityService = securityService;
  }

  /**
   * Finds all detection events of a cheating detection.
   *
   * @param cheatingDetectionId the cheating detection ID
   * @param pageable the pageable
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isOrganizerOfGivenTrainingInstance(#trainingInstanceId)")
  @TransactionalWO
  public PageResultResource<AbstractDetectionEventDTO> findAllDetectionEventsOfCheatingDetection(
      Long cheatingDetectionId, Pageable pageable, Predicate predicate, Long trainingInstanceId) {
    return detectionEventMapper.mapToPageResultResource(
        this.detectionEventService.findAllDetectionEventsOfCheatingDetection(
            cheatingDetectionId, pageable, predicate));
  }

  /**
   * Finds all participants of detection event.
   *
   * @param eventId the detection event ID
   * @param pageable the pageable
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)")
  @TransactionalWO
  public PageResultResource<DetectionEventParticipantDTO> findAllParticipantsOfDetectionEvent(
      Long eventId, Pageable pageable) {
    return detectionEventParticipantMapper.mapToPageResultResource(
        this.detectionEventService.findAllParticipantsOfEvent(eventId, pageable));
  }

  /**
   * Finds all forbidden commands of detection event.
   *
   * @param eventId the detection event ID
   * @param pageable the pageable
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)")
  @TransactionalWO
  public PageResultResource<DetectedForbiddenCommandDTO> findAllForbiddenCommandsOfDetectionEvent(
      Long eventId, Pageable pageable) {
    return detectedForbiddenCommandMapper.mapToPageResultResource(
        this.detectionEventService.findAllForbiddenCommandsOfDetectionEvent(eventId, pageable));
  }

  /**
   * Finds all forbidden commands of detection event for visualization.
   *
   * @param eventId the detection event ID
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)")
  @TransactionalWO
  public List<DetectedForbiddenCommandDTO> findAllForbiddenCommandsOfDetectionEvent(Long eventId) {
    return detectedForbiddenCommandMapper.mapToListDTO(
        this.detectionEventService.findAllForbiddenCommandsOfDetectionEvent(eventId));
  }

  /**
   * Find detection event by its ID.
   *
   * @param eventId the detection event ID
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)")
  @TransactionalWO
  public AbstractDetectionEventDTO findDetectionEventById(Long eventId) {
    return detectionEventMapper.mapToDTO(
        this.detectionEventService.findDetectionEventById(eventId));
  }

  /**
   * Find detection event of type answer similarity by its ID.
   *
   * @param eventId the detection event ID
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)")
  @TransactionalWO
  public AnswerSimilarityDetectionEventDTO findAnswerSimilarityEventById(Long eventId) {
    return detectionEventMapper.mapToAnswerSimilarityDetectionEventDTO(
        this.answerSimilarityService.findAnswerSimilarityEventById(eventId));
  }

  /**
   * Find detection event of type location similarity by its ID.
   *
   * @param eventId the detection event ID
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)")
  @TransactionalWO
  public LocationSimilarityDetectionEventDTO findLocationSimilarityEventById(Long eventId) {
    return detectionEventMapper.mapToLocationSimilarityDetectionEventDTO(
        this.locationSimilarityService.findLocationSimilarityEventById(eventId));
  }

  /**
   * Find detection event of type time proximity by its ID.
   *
   * @param eventId the detection event ID
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)")
  @TransactionalWO
  public TimeProximityDetectionEventDTO findTimeProximityEventById(Long eventId) {
    return detectionEventMapper.mapToTimeProximityDetectionEventDTO(
        this.timeProximityService.findTimeProximityEventById(eventId));
  }

  /**
   * Find detection event of type minimal solve time by its ID.
   *
   * @param eventId the detection event ID
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)")
  @TransactionalWO
  public MinimalSolveTimeDetectionEventDTO findMinimalSolveTimeEventById(Long eventId) {
    return detectionEventMapper.mapToMinimalSolveTimeDetectionEventDTO(
        this.minimalSolveTimeService.findMinimalSolveTimeEventById(eventId));
  }

  /**
   * Find detection event of type no commands by its ID.
   *
   * @param eventId the detection event ID
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)")
  @TransactionalWO
  public NoCommandsDetectionEventDTO findNoCommandsEventById(Long eventId) {
    return detectionEventMapper.mapToNoCommandsDetectionEventDTO(
        this.noCommandsService.findNoCommandsEventById(eventId));
  }

  /**
   * Find detection event of type forbidden commands by its ID.
   *
   * @param eventId the detection event ID
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)")
  @TransactionalWO
  public ForbiddenCommandsDetectionEventDTO findForbiddenCommandsEventById(Long eventId) {
    return detectionEventMapper.mapToForbiddenCommandsDetectionEventDTO(
        this.forbiddenCommandsService.findForbiddenCommandsEventById(eventId));
  }
}
