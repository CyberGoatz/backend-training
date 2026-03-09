package cz.cyberrange.platform.training.service.facade;

import cz.cyberrange.platform.training.api.dto.UserRefDTO;
import cz.cyberrange.platform.training.api.dto.visualization.compact.CompactLevelViewDTO;
import cz.cyberrange.platform.training.api.dto.visualization.compact.CompactLevelViewEventDTO;
import cz.cyberrange.platform.training.api.dto.visualization.compact.CompactLevelViewUserDTO;
import cz.cyberrange.platform.training.opensearch.model.AbstractAuditPOJO;
import cz.cyberrange.platform.training.opensearch.model.TrainingRunEnded;
import cz.cyberrange.platform.training.opensearch.model.TrainingRunStarted;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import cz.cyberrange.platform.training.service.annotations.transactions.TransactionalRO;
import cz.cyberrange.platform.training.service.services.TrainingDefinitionService;
import cz.cyberrange.platform.training.service.services.TrainingInstanceService;
import cz.cyberrange.platform.training.service.services.TrainingRunService;
import cz.cyberrange.platform.training.service.services.UserService;
import cz.cyberrange.platform.training.service.services.api.OpenSearchApiService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CompactLevelViewFacade {

    private final TrainingDefinitionService trainingDefinitionService;
    private final TrainingInstanceService trainingInstanceService;
    private final TrainingRunService trainingRunService;
    private final UserService userService;
    private final OpenSearchApiService opensearchApiService;


    public CompactLevelViewFacade(TrainingDefinitionService trainingDefinitionService,
                                  TrainingInstanceService trainingInstanceService,
                                  TrainingRunService trainingRunService,
                                  UserService userService,
                                  OpenSearchApiService opensearchApiService) {
        this.trainingDefinitionService = trainingDefinitionService;
        this.trainingInstanceService = trainingInstanceService;
        this.trainingRunService = trainingRunService;
        this.userService = userService;
        this.opensearchApiService = opensearchApiService;
    }

    @PreAuthorize("hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#instanceId)")
    @TransactionalRO
    public CompactLevelViewDTO getCompactLevelViewData(Long instanceId, Long levelId) {
        //check if level exists and retrieve the level title
        String title = trainingDefinitionService.findLevelById(levelId).getTitle();

        CompactLevelViewDTO compactLevelViewDTO = new CompactLevelViewDTO(levelId);
        compactLevelViewDTO.setTitle(title);
        TrainingInstance instance = trainingInstanceService.findById(instanceId);
        Set<TrainingRun> trainingRuns = trainingRunService.findAllByTrainingInstanceId(instanceId);
        Map<Long, List<AbstractAuditPOJO>> levelEventsByUserId = opensearchApiService.getEventsOfTrainingInstanceAndLevelAggregatedByUsers(instanceId, levelId);
        Map<Long, UserRefDTO> usersByIds = userService.getUsersRefDTOByGivenUserIds(new ArrayList<>(levelEventsByUserId.keySet())).stream()
                .collect(Collectors.toMap(UserRefDTO::getUserRefId, Function.identity()));

        for(TrainingRun trainingRun : trainingRuns) {
            List<AbstractAuditPOJO> userLevelEvents = levelEventsByUserId.getOrDefault(trainingRun.getParticipantRef().getUserRefId(), new ArrayList<>());
            if (userLevelEvents.isEmpty()) {
                continue;
            }
            List<Map<String, Object>> userLevelCommands = getUserLevelCommands(instance, trainingRun, userLevelEvents);

            CompactLevelViewUserDTO compactLevelViewUserDTO = new CompactLevelViewUserDTO();
            compactLevelViewUserDTO.setUser(usersByIds.get(trainingRun.getParticipantRef().getUserRefId()));
            compactLevelViewUserDTO.setEvents(getCompactLevelViewEvents(userLevelEvents, userLevelCommands));
            compactLevelViewDTO.addUser(compactLevelViewUserDTO);
        }
        return compactLevelViewDTO;
    }

    private List<CompactLevelViewEventDTO> getCompactLevelViewEvents(List<AbstractAuditPOJO> userLevelEvents, List<Map<String, Object>> levelCommands) {
        List<CompactLevelViewEventDTO> result = new ArrayList<>();
        for (AbstractAuditPOJO event : userLevelEvents) {
            if(event.getClass() == TrainingRunStarted.class || event.getClass() == TrainingRunEnded.class) {
                continue;
            }
            CompactLevelViewEventDTO eventDTO = new CompactLevelViewEventDTO();
            eventDTO.setTimestamp(event.getTimestamp());
            eventDTO.setType(event.getType());
            List<String> commandsUpToEvent = levelCommands.stream()
                    .takeWhile(cmd -> ZonedDateTime.parse((String) cmd.get("timestamp_str")).toInstant().toEpochMilli() < event.getTimestamp())
                    .map(cmd -> (String) cmd.get("cmd"))
                    .toList();
            eventDTO.setCommands(commandsUpToEvent);
            levelCommands = levelCommands.subList(commandsUpToEvent.size(), levelCommands.size());

            result.add(eventDTO);
        }
        return result;
    }

    private List<Map<String, Object>> getUserLevelCommands(TrainingInstance instance, TrainingRun run, List<AbstractAuditPOJO> userLevelEvents) {
        Long from = userLevelEvents.get(0).getTimestamp();
        Long to = userLevelEvents.get(userLevelEvents.size() - 1).getTimestamp();
        if(instance.isLocalEnvironment()) {
            return opensearchApiService.findAllConsoleCommandsByAccessTokenAndUserIdAndTimeRange(instance.getAccessToken(), run.getParticipantRef().getUserRefId(), from, to);
        }
        return opensearchApiService.findAllConsoleCommandsBySandboxAndTimeRange(run.getSandboxInstanceRefId(), from, to);
    }
}
