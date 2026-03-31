package cz.cyberrange.platform.training.service.services.detection;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.cyberrange.platform.training.api.dto.UserRefDTO;
import cz.cyberrange.platform.training.api.dto.cheatingdetection.CheatingDetectionDTO;
import cz.cyberrange.platform.training.persistence.model.detection.AbstractDetectionEvent;
import cz.cyberrange.platform.training.persistence.model.detection.AnswerSimilarityDetectionEvent;
import cz.cyberrange.platform.training.persistence.model.detection.DetectedForbiddenCommand;
import cz.cyberrange.platform.training.persistence.model.detection.DetectionEventParticipant;
import cz.cyberrange.platform.training.persistence.model.detection.ForbiddenCommandsDetectionEvent;
import cz.cyberrange.platform.training.persistence.model.detection.LocationSimilarityDetectionEvent;
import cz.cyberrange.platform.training.persistence.model.detection.MinimalSolveTimeDetectionEvent;
import cz.cyberrange.platform.training.persistence.model.detection.NoCommandsDetectionEvent;
import cz.cyberrange.platform.training.persistence.model.detection.TimeProximityDetectionEvent;
import cz.cyberrange.platform.training.persistence.model.enums.DetectionEventType;
import cz.cyberrange.platform.training.service.services.TrainingDefinitionService;
import cz.cyberrange.platform.training.service.services.UserService;
import cz.cyberrange.platform.training.service.utils.AbstractFileExtensions;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CheatingDetectionExportService {
  private static final String DETECTION_EVENTS_FOLDER = "detection_events";
  private static final String ANSWER_SIMILARITY_FOLDER = "answer_similarity";
  private static final String LOCATION_SIMILARITY_FOLDER = "location_similarity";
  private static final String TIME_PROXIMITY_FOLDER = "time_proximity";
  private static final String MINIMAL_SOLVE_TIME_FOLDER = "minimal_solve_time";
  private static final String NO_COMMANDS_FOLDER = "no_commands";
  private static final String FORBIDDEN_COMMANDS_FOLDER = "forbidden_commands";
  private static final String PARTICIPANT_RESPONSE_FOLDER = "participant_groups";

  private static final Logger LOG = LoggerFactory.getLogger(CheatingDetectionService.class);
  private final CheatingDetectionService cheatingDetectionService;
  private final DetectionEventService detectionEventService;
  private final AnswerSimilarityService answerSimilarityService;
  private final LocationSimilarityService locationSimilarityService;
  private final MinimalSolveTimeService minimalSolveTimeService;
  private final TimeProximityService timeProximityService;
  private final NoCommandsService noCommandsService;
  private final ForbiddenCommandsService forbiddenCommandsService;
  public final UserService userService;
  private final TrainingDefinitionService trainingDefinitionService;
  private final ObjectMapper objectMapper;
  private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  /**
   * Instantiates a new Cheating detection facade.
   *
   * @param cheatingDetectionService the cheating detection service
   * @param trainingDefinitionService the training definition service
   * @param userService the user service
   * @param detectionEventService the detection event service
   * @param answerSimilarityService the answer similarity service
   * @param locationSimilarityService the location similarity service
   * @param minimalSolveTimeService the minimal solve time service
   * @param timeProximityService the time proximity service
   * @param noCommandsService the no commands service
   * @param forbiddenCommandsService the forbidden commands service
   */
  @Autowired
  public CheatingDetectionExportService(
      CheatingDetectionService cheatingDetectionService,
      UserService userService,
      TrainingDefinitionService trainingDefinitionService,
      DetectionEventService detectionEventService,
      AnswerSimilarityService answerSimilarityService,
      LocationSimilarityService locationSimilarityService,
      MinimalSolveTimeService minimalSolveTimeService,
      TimeProximityService timeProximityService,
      NoCommandsService noCommandsService,
      ForbiddenCommandsService forbiddenCommandsService,
      ObjectMapper objectMapper) {
    this.cheatingDetectionService = cheatingDetectionService;
    this.userService = userService;
    this.trainingDefinitionService = trainingDefinitionService;
    this.detectionEventService = detectionEventService;
    this.answerSimilarityService = answerSimilarityService;
    this.locationSimilarityService = locationSimilarityService;
    this.minimalSolveTimeService = minimalSolveTimeService;
    this.timeProximityService = timeProximityService;
    this.noCommandsService = noCommandsService;
    this.forbiddenCommandsService = forbiddenCommandsService;
    this.objectMapper = objectMapper;
  }

  public void writeCheatingDetection(
      ZipOutputStream zos, Long cheatingDetectionId, CheatingDetectionDTO cheatingDetectionDTO)
      throws IOException {
    ZipEntry cheatingDetectionEntry =
        new ZipEntry(
            "cheating-detection-id"
                + cheatingDetectionId
                + AbstractFileExtensions.JSON_FILE_EXTENSION);
    zos.putNextEntry(cheatingDetectionEntry);
    zos.write(objectMapper.writeValueAsBytes(cheatingDetectionDTO));
  }

  public void writeAnswerSimilarityDetectionEvents(ZipOutputStream zos, Long cheatingDetectionId)
      throws IOException {
    List<AnswerSimilarityDetectionEvent> detectionEventsOfAS =
        answerSimilarityService.findAllAnswerSimilarityEventsOfDetection(cheatingDetectionId);
    for (var event : detectionEventsOfAS) {
      writeDetectionEventToFile(zos, event, ANSWER_SIMILARITY_FOLDER);
    }
  }

  public void writeLocationSimilarityDetectionEvents(ZipOutputStream zos, Long cheatingDetectionId)
      throws IOException {
    List<LocationSimilarityDetectionEvent> detectionEventsOfLS =
        locationSimilarityService.findAllLocationSimilarityEventsOfDetection(cheatingDetectionId);
    for (var event : detectionEventsOfLS) {
      writeDetectionEventToFile(zos, event, LOCATION_SIMILARITY_FOLDER);
    }
  }

  public void writeTimeProximityDetectionEvents(ZipOutputStream zos, Long cheatingDetectionId)
      throws IOException {
    List<TimeProximityDetectionEvent> detectionEventsOfTP =
        timeProximityService.findAllTimeProximityEventsOfDetection(cheatingDetectionId);
    for (var event : detectionEventsOfTP) {
      writeDetectionEventToFile(zos, event, TIME_PROXIMITY_FOLDER);
    }
  }

  public void writeMinimalSolveTimeDetectionEvents(ZipOutputStream zos, Long cheatingDetectionId)
      throws IOException {
    List<MinimalSolveTimeDetectionEvent> detectionEventsOfMST =
        minimalSolveTimeService.findAllMinimalSolveTimeEventsOfDetection(cheatingDetectionId);
    for (var event : detectionEventsOfMST) {
      writeDetectionEventToFile(zos, event, MINIMAL_SOLVE_TIME_FOLDER);
    }
  }

  public void writeNoCommandsDetectionEvents(ZipOutputStream zos, Long cheatingDetectionId)
      throws IOException {
    List<NoCommandsDetectionEvent> detectionEventsOfNC =
        noCommandsService.findAllNoCommandsEventsOfDetection(cheatingDetectionId);
    for (var event : detectionEventsOfNC) {
      writeDetectionEventToFile(zos, event, NO_COMMANDS_FOLDER);
    }
  }

  public void writeForbiddenCommandsDetectionEvents(ZipOutputStream zos, Long cheatingDetectionId)
      throws IOException {
    List<ForbiddenCommandsDetectionEvent> detectionEventsOfFC =
        forbiddenCommandsService.findAllForbiddenCommandsEventsOfDetection(cheatingDetectionId);
    for (var event : detectionEventsOfFC) {
      writeDetectionEventToFile(zos, event, FORBIDDEN_COMMANDS_FOLDER);
    }
  }

  private void writeDetectionEventToFile(
      ZipOutputStream zos, AbstractDetectionEvent event, String dirName) throws IOException {
    ZipEntry detectionEventEntry =
        new ZipEntry(
            DETECTION_EVENTS_FOLDER
                + "/"
                + dirName
                + "/detection-event-id"
                + event.getId()
                + AbstractFileExtensions.JSON_FILE_EXTENSION);
    zos.putNextEntry(detectionEventEntry);
    List<DetectionEventParticipant> participants =
        detectionEventService.findAllParticipantsOfEvent(event.getId());
    zos.write(objectMapper.writeValueAsBytes(event));
    ZipEntry detectionEventParticipantsEntry =
        new ZipEntry(
            DETECTION_EVENTS_FOLDER
                + "/"
                + dirName
                + "/detection-event-id"
                + event.getId()
                + "-participants"
                + AbstractFileExtensions.JSON_FILE_EXTENSION);
    zos.putNextEntry(detectionEventParticipantsEntry);
    zos.write(objectMapper.writeValueAsBytes(participants));
  }

  public void writeTraineeParticipantGroups(ZipOutputStream zos, Long cheatingDetectionId)
      throws IOException {
    List<DetectionEventParticipant> participants =
        cheatingDetectionService
            .findAllParticipantsOfCheatingDetection(cheatingDetectionId)
            .stream()
            .filter(
                participant -> {
                  var participantEventType =
                      detectionEventService
                          .findDetectionEventById(participant.getDetectionEventId())
                          .getDetectionEventType();
                  return participantEventType != DetectionEventType.MINIMAL_SOLVE_TIME;
                })
            .collect(Collectors.toList());
    Map<Long, Set<Long>> eventsByParticipants = mapUsersToEvents(participants);
    Map<List<Long>, Set<Long>> participantGroups = createParticipantGroups(eventsByParticipants);

    for (Map.Entry<List<Long>, Set<Long>> entry : participantGroups.entrySet()) {
      List<Long> userGroup = entry.getKey();
      Set<Long> eventGroup = entry.getValue();

      StringBuilder usersString = new StringBuilder();
      for (var userId : userGroup) {
        usersString.append(userId).append('_');
      }
      if (usersString.length() > 0) {
        usersString.deleteCharAt(usersString.length() - 1);
      }
      ZipEntry participantResponseEntry =
          new ZipEntry(
              PARTICIPANT_RESPONSE_FOLDER
                  + "/"
                  + usersString
                  + AbstractFileExtensions.CSV_FILE_EXTENSION);
      zos.putNextEntry(participantResponseEntry);
      List<MinimalSolveTimeDetectionEvent> minimalSolveTimeEvents =
          minimalSolveTimeService.findAllMinimalSolveTimeEventsOfGroup(
              cheatingDetectionId, userGroup);
      auditParticipants(userGroup, zos);
      auditParticipantGroupEvents(eventGroup, zos);
      for (var event : minimalSolveTimeEvents) {
        List<DetectionEventParticipant> eventParticipants =
            detectionEventService.findAllParticipantsOfEvent(event.getId());
        List<DetectionEventParticipant> relevantParticipants = new ArrayList<>();
        for (var participant : eventParticipants) {
          if (userGroup.contains(participant.getUserId())) {
            relevantParticipants.add(participant);
          }
        }
        auditMinimalSolveTimeGroup(relevantParticipants, event, zos);
      }
    }
  }

  private Map<Long, Set<Long>> mapUsersToEvents(List<DetectionEventParticipant> participants) {
    Map<Long, Set<Long>> userEventMap = new HashMap<>();
    for (DetectionEventParticipant participant : participants) {
      Long userId = participant.getUserId();
      Set<Long> eventIds = userEventMap.getOrDefault(userId, new HashSet<>());
      eventIds.add(participant.getDetectionEventId());
      userEventMap.put(userId, eventIds);
    }
    return userEventMap;
  }

  private void dfs(
      Map<Long, Set<Long>> userEventMap,
      Long userId,
      Set<Long> visitedUsers,
      List<Long> userGroup,
      Set<Long> eventGroup) {
    visitedUsers.add(userId);
    userGroup.add(userId);
    Set<Long> events = userEventMap.get(userId);
    if (events == null) {
      return;
    }
    eventGroup.addAll(events);
    for (Long eventId : events) {
      for (Map.Entry<Long, Set<Long>> entry : userEventMap.entrySet()) {
        Long nextUserId = entry.getKey();
        if (!visitedUsers.contains(nextUserId) && entry.getValue().contains(eventId)) {
          dfs(userEventMap, nextUserId, visitedUsers, userGroup, eventGroup);
        }
      }
    }
  }

  private Map<List<Long>, Set<Long>> createParticipantGroups(Map<Long, Set<Long>> userEventMap) {
    Map<List<Long>, Set<Long>> participantGroups = new HashMap<>();
    Set<Long> visitedUsers = new HashSet<>();

    for (Map.Entry<Long, Set<Long>> entry : userEventMap.entrySet()) {
      Long userId = entry.getKey();
      if (!visitedUsers.contains(userId)) {
        List<Long> userGroup = new ArrayList<>();
        Set<Long> eventGroup = new HashSet<>();
        dfs(userEventMap, userId, visitedUsers, userGroup, eventGroup);
        participantGroups.put(userGroup, eventGroup);
      }
    }
    return participantGroups;
  }

  private void auditParticipants(List<Long> userIds, ZipOutputStream zos) throws IOException {
    StringBuilder csvData = new StringBuilder();

    csvData.append("GROUP PARTICIPANTS\n");
    csvData.append("user id,name,ref\n");

    for (var userId : userIds) {
      UserRefDTO user = userService.getUserRefDTOByUserRefId(userId);
      csvData.append(
          String.format("%s,%s,%s\n", userId, user.getUserRefFullName(), user.getUserRefSub()));
    }
    csvData.append("\n\nEVENTS\n\n");
    byte[] bytes = csvData.toString().getBytes();
    zos.write(bytes, 0, bytes.length);
  }

  private void auditParticipantGroupEvents(Set<Long> eventIds, ZipOutputStream zos)
      throws IOException {
    var first = true;
    for (var eventId : eventIds) {
      AbstractDetectionEvent event = detectionEventService.findDetectionEventById(eventId);
      if (first) {
        writeDetectedAt(event, zos);
        first = false;
      }
      var eventType = event.getDetectionEventType();
      List<DetectionEventParticipant> participants =
          detectionEventService.findAllParticipantsOfEvent(eventId);
      switch (eventType) {
        case ANSWER_SIMILARITY ->
            auditAnswerSimilarityGroup(
                participants, answerSimilarityService.findAnswerSimilarityEventById(eventId), zos);
        case LOCATION_SIMILARITY ->
            auditLocationSimilarityGroup(
                participants,
                locationSimilarityService.findLocationSimilarityEventById(eventId),
                zos);
        case TIME_PROXIMITY ->
            auditTimeProximityGroup(
                participants, timeProximityService.findTimeProximityEventById(eventId), zos);
        case NO_COMMANDS ->
            auditNoCommandsGroup(
                participants, noCommandsService.findNoCommandsEventById(eventId), zos);
        case FORBIDDEN_COMMANDS ->
            auditForbiddenCommandsGroup(
                participants,
                forbiddenCommandsService.findForbiddenCommandsEventById(eventId),
                zos);
      }
    }
  }

  private void writeDetectedAt(AbstractDetectionEvent event, ZipOutputStream zos)
      throws IOException {
    byte[] bytes =
        String.format("detected at:,%s\n", event.getDetectedAt().format(formatter)).getBytes();
    zos.write(bytes, 0, bytes.length);
  }

  private void auditAnswerSimilarityGroup(
      List<DetectionEventParticipant> participants,
      AnswerSimilarityDetectionEvent event,
      ZipOutputStream zos)
      throws IOException {
    StringBuilder csvData = new StringBuilder();

    csvData.append("\nANSWER SIMILARITY EVENT\n");
    csvData.append("level order,level title,answer,answer owner\n");
    int order = trainingDefinitionService.findLevelById(event.getLevelId()).getOrder();
    csvData.append(
        String.format(
            "%s,%s,%s,%s\n",
            order, event.getLevelTitle(), event.getAnswer(), event.getAnswerOwner()));

    if (participants.size() > 1) {
      csvData.append("\nPARTICIPANTS\n");
      csvData.append("participant,time\n");
      for (var participant : participants) {
        csvData.append(
            String.format(
                "%s,%s\n",
                participant.getParticipantName(), participant.getOccurredAt().format(formatter)));
      }
    }
    csvData.append("\n\n\n");
    byte[] bytes = csvData.toString().getBytes();
    zos.write(bytes, 0, bytes.length);
  }

  private void auditLocationSimilarityGroup(
      List<DetectionEventParticipant> participants,
      LocationSimilarityDetectionEvent event,
      ZipOutputStream zos)
      throws IOException {
    StringBuilder csvData = new StringBuilder();

    csvData.append("\nLOCATION SIMILARITY EVENT\n");
    csvData.append("level order,level title,hostname,IP\n");
    int order = trainingDefinitionService.findLevelById(event.getLevelId()).getOrder();
    csvData.append(
        String.format(
            "%s,%s,%s,%s\n", order, event.getLevelTitle(), event.getDns(), event.getIpAddress()));

    if (participants.size() > 1) {
      csvData.append("\nPARTICIPANTS\n");
      csvData.append("participant,time,IP\n");
      for (var participant : participants) {
        csvData.append(
            String.format(
                "%s,%s,%s\n",
                participant.getParticipantName(),
                participant.getOccurredAt().format(formatter),
                participant.getIpAddress()));
      }
    }
    csvData.append("\n\n\n");
    byte[] bytes = csvData.toString().getBytes();
    zos.write(bytes, 0, bytes.length);
  }

  private void auditMinimalSolveTimeGroup(
      List<DetectionEventParticipant> participants,
      MinimalSolveTimeDetectionEvent event,
      ZipOutputStream zos)
      throws IOException {
    StringBuilder csvData = new StringBuilder();

    csvData.append("\nMINIMAL SOLVE TIME EVENT\n");
    csvData.append("level order,level title,minimal solve time\n");
    int order = trainingDefinitionService.findLevelById(event.getLevelId()).getOrder();
    csvData.append(
        String.format("%s,%s,%s\n", order, event.getLevelTitle(), event.getMinimalSolveTime()));

    if (participants.size() > 1) {
      csvData.append("\nPARTICIPANTS\n");
      csvData.append("participant,time,solved in (seconds)\n");
      for (var participant : participants) {
        csvData.append(
            String.format(
                "%s,%s,%s\n",
                participant.getParticipantName(),
                participant.getOccurredAt().format(formatter),
                participant.getSolvedInTime()));
      }
    }
    csvData.append("\n\n\n");
    byte[] bytes = csvData.toString().getBytes();
    zos.write(bytes, 0, bytes.length);
  }

  private void auditTimeProximityGroup(
      List<DetectionEventParticipant> participants,
      TimeProximityDetectionEvent event,
      ZipOutputStream zos)
      throws IOException {
    StringBuilder csvData = new StringBuilder();

    csvData.append("\nTIME PROXIMITY EVENT\n");
    csvData.append("level order,level title,proximity\n");
    int order = trainingDefinitionService.findLevelById(event.getLevelId()).getOrder();
    csvData.append(String.format("%s,%s,%s\n", order, event.getLevelTitle(), event.getThreshold()));

    if (participants.size() > 1) {
      csvData.append("\nPARTICIPANTS\n");
      csvData.append("participant,time\n");
      for (var participant : participants) {
        csvData.append(
            String.format(
                "%s,%s\n",
                participant.getParticipantName(), participant.getOccurredAt().format(formatter)));
      }
    }
    csvData.append("\n\n\n");
    byte[] bytes = csvData.toString().getBytes();
    zos.write(bytes, 0, bytes.length);
  }

  private void auditNoCommandsGroup(
      List<DetectionEventParticipant> participants,
      NoCommandsDetectionEvent event,
      ZipOutputStream zos)
      throws IOException {
    StringBuilder csvData = new StringBuilder();

    csvData.append("\nNO COMMANDS EVENT\n");
    csvData.append("level order,level title\n");
    int order = trainingDefinitionService.findLevelById(event.getLevelId()).getOrder();
    csvData.append(String.format("%s,%s\n", order, event.getLevelTitle()));

    if (participants.size() > 1) {
      csvData.append("\nPARTICIPANTS\n");
      csvData.append("participant,time\n");
      for (var participant : participants) {
        csvData.append(
            String.format(
                "%s,%s\n",
                participant.getParticipantName(), participant.getOccurredAt().format(formatter)));
      }
    }
    csvData.append("\n\n\n");
    byte[] bytes = csvData.toString().getBytes();
    zos.write(bytes, 0, bytes.length);
  }

  private void auditForbiddenCommandsGroup(
      List<DetectionEventParticipant> participants,
      ForbiddenCommandsDetectionEvent event,
      ZipOutputStream zos)
      throws IOException {
    StringBuilder csvData = new StringBuilder();

    csvData.append("\nFORBIDDEN COMMANDS EVENT\n");
    csvData.append("level order,level title\n");
    int order = trainingDefinitionService.findLevelById(event.getLevelId()).getOrder();
    csvData.append(String.format("%s,%s\n", order, event.getLevelTitle()));

    if (participants.size() > 1) {
      csvData.append("\nPARTICIPANTS\n");
      csvData.append("participant,time\n");
      for (var participant : participants) {
        csvData.append(
            String.format(
                "%s,%s\n",
                participant.getParticipantName(), participant.getOccurredAt().format(formatter)));
      }
    }
    csvData.append("\nFORBIDDEN COMMANDS\n");
    csvData.append("command,type,hostname,time\n");
    List<DetectedForbiddenCommand> commands =
        detectionEventService.findAllForbiddenCommandsOfDetectionEvent(event.getId());
    for (var command : commands) {
      csvData.append(
          String.format(
              "%s,%s,%s,%s\n",
              command.getCommand(),
              command.getType(),
              command.getHostname(),
              command.getOccurredAt().format(formatter)));
    }
    csvData.append("\n\n\n");
    byte[] bytes = csvData.toString().getBytes();
    zos.write(bytes, 0, bytes.length);
  }
}
