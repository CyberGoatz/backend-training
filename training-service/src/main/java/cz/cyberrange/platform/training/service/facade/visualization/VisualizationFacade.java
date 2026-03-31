package cz.cyberrange.platform.training.service.facade.visualization;

import cz.cyberrange.platform.training.api.dto.UserRefDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionMitreTechniquesDTO;
import cz.cyberrange.platform.training.api.dto.visualization.AbstractLevelVisualizationDTO;
import cz.cyberrange.platform.training.api.dto.visualization.VisualizationInfoDTO;
import cz.cyberrange.platform.training.api.dto.visualization.clustering.ClusteringLevelDTO;
import cz.cyberrange.platform.training.api.dto.visualization.clustering.ClusteringLevelPlayerDTO;
import cz.cyberrange.platform.training.api.dto.visualization.clustering.ClusteringVisualizationDTO;
import cz.cyberrange.platform.training.api.dto.visualization.clustering.TrainingResultsDTO;
import cz.cyberrange.platform.training.api.dto.visualization.clustering.TrainingResultsPlayerDTO;
import cz.cyberrange.platform.training.api.dto.visualization.commons.EventDTO;
import cz.cyberrange.platform.training.api.dto.visualization.commons.PlayerDataDTO;
import cz.cyberrange.platform.training.api.dto.visualization.commons.VisualizationAbstractLevelDTO;
import cz.cyberrange.platform.training.api.dto.visualization.leveltabs.LevelTabsHintDTO;
import cz.cyberrange.platform.training.api.dto.visualization.leveltabs.LevelTabsLevelDTO;
import cz.cyberrange.platform.training.api.dto.visualization.leveltabs.LevelTabsPlayerDTO;
import cz.cyberrange.platform.training.api.dto.visualization.progress.LevelDefinitionProgressDTO;
import cz.cyberrange.platform.training.api.dto.visualization.progress.LevelProgressDTO;
import cz.cyberrange.platform.training.api.dto.visualization.progress.ProgressDTO;
import cz.cyberrange.platform.training.api.dto.visualization.progress.VisualizationProgressDTO;
import cz.cyberrange.platform.training.api.dto.visualization.table.TableLevelDTO;
import cz.cyberrange.platform.training.api.dto.visualization.table.TablePlayerDTO;
import cz.cyberrange.platform.training.api.dto.visualization.timeline.TimelineDTO;
import cz.cyberrange.platform.training.api.dto.visualization.timeline.TimelineLevelDTO;
import cz.cyberrange.platform.training.api.dto.visualization.timeline.TimelinePlayerDTO;
import cz.cyberrange.platform.training.api.enums.AssessmentType;
import cz.cyberrange.platform.training.api.enums.LevelState;
import cz.cyberrange.platform.training.api.responses.PageResultResource;
import cz.cyberrange.platform.training.api.responses.SandboxAnswersInfo;
import cz.cyberrange.platform.training.api.responses.VariantAnswer;
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
import cz.cyberrange.platform.training.opensearch.query.service.TrainingCommandEventsService;
import cz.cyberrange.platform.training.persistence.model.AbstractLevel;
import cz.cyberrange.platform.training.persistence.model.AccessLevel;
import cz.cyberrange.platform.training.persistence.model.AssessmentLevel;
import cz.cyberrange.platform.training.persistence.model.InfoLevel;
import cz.cyberrange.platform.training.persistence.model.MitreTechnique;
import cz.cyberrange.platform.training.persistence.model.TrainingDefinition;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.persistence.model.TrainingLevel;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import cz.cyberrange.platform.training.persistence.model.enums.TDState;
import cz.cyberrange.platform.training.service.annotations.security.IsDesignerOrOrganizerOrAdmin;
import cz.cyberrange.platform.training.service.annotations.security.IsTrainee;
import cz.cyberrange.platform.training.service.annotations.security.IsTraineeOrAdmin;
import cz.cyberrange.platform.training.service.annotations.transactions.TransactionalRO;
import cz.cyberrange.platform.training.service.annotations.transactions.TransactionalWO;
import cz.cyberrange.platform.training.service.mapping.mapstruct.LevelMapper;
import cz.cyberrange.platform.training.service.services.TrainingDefinitionService;
import cz.cyberrange.platform.training.service.services.TrainingInstanceService;
import cz.cyberrange.platform.training.service.services.TrainingRunService;
import cz.cyberrange.platform.training.service.services.UserService;
import cz.cyberrange.platform.training.service.services.VisualizationService;
import cz.cyberrange.platform.training.service.services.api.AnswersStorageApiService;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class VisualizationFacade {

  private static final Logger LOG = LoggerFactory.getLogger(VisualizationFacade.class);

  private final TrainingDefinitionService trainingDefinitionService;
  private final TrainingInstanceService trainingInstanceService;
  private final TrainingRunService trainingRunService;
  private final VisualizationService visualizationService;
  private final AnswersStorageApiService answersStorageApiService;
  private final TrainingCommandEventsService trainingCommandEventsService;
  private final UserService userService;
  private final LevelMapper levelMapper;

  /**
   * Instantiates a new Visualization facade.
   *
   * @param trainingRunService the training run service
   * @param trainingInstanceService the training instance service
   * @param visualizationService the visualization service
   * @param levelMapper the level mapper
   * @param userService the user service
   */
  @Autowired
  public VisualizationFacade(
      TrainingDefinitionService trainingDefinitionService,
      TrainingInstanceService trainingInstanceService,
      TrainingRunService trainingRunService,
      VisualizationService visualizationService,
      AnswersStorageApiService answersStorageApiService,
      TrainingCommandEventsService trainingCommandsService,
      UserService userService,
      LevelMapper levelMapper) {
    this.trainingDefinitionService = trainingDefinitionService;
    this.trainingInstanceService = trainingInstanceService;
    this.trainingRunService = trainingRunService;
    this.answersStorageApiService = answersStorageApiService;
    this.visualizationService = visualizationService;
    this.trainingCommandEventsService = trainingCommandsService;
    this.levelMapper = levelMapper;
    this.userService = userService;
  }

  /**
   * Gather all the necessary information about the users with given ids.
   *
   * @param usersIds ids of the users to be retrieved.
   * @param pageable pageable parameter with information about pagination.
   * @return basic info about the users with given ids.
   */
  @IsDesignerOrOrganizerOrAdmin
  public PageResultResource<UserRefDTO> getUsersByIds(Set<Long> usersIds, Pageable pageable) {
    return userService.getUsersRefDTOByGivenUserIds(
        new ArrayList<>(usersIds), pageable, null, null);
  }

  /**
   * Gather all the necessary information about the training run needed to visualize the result.
   *
   * @param trainingRunId id of Training Run to gets info.
   * @return basic info about the training definition of given a training run and the necessary info
   *     about all levels from that training run.
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isTraineeOfGivenTrainingRun(#trainingRunId)")
  @TransactionalWO
  public VisualizationInfoDTO getVisualizationInfoAboutTrainingRun(Long trainingRunId) {
    TrainingRun trainingRun = trainingRunService.findByIdWithLevel(trainingRunId);
    TrainingDefinition trainingDefinitionOfTrainingRun =
        trainingRun.getTrainingInstance().getTrainingDefinition();
    return new VisualizationInfoDTO(
        trainingDefinitionOfTrainingRun.getId(),
        trainingDefinitionOfTrainingRun.getTitle(),
        trainingDefinitionOfTrainingRun.getEstimatedDuration(),
        convertToAbstractLevelVisualizationDTO(
            visualizationService.getLevelsForTraineeVisualization(trainingRun)));
  }

  /**
   * Gather all the necessary information about the training run needed to visualize the result.
   *
   * @param trainingRunId id of Training Run to gets info.
   * @return the list of all commands in a sandbox.
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isOrganizerOfGivenTrainingInstance(#instanceId)"
          + "or @securityService.isTraineeOfGivenTrainingRun(#trainingRunId)")
  @TransactionalWO
  public List<Map<String, Object>> getAllCommandsInTrainingRun(
      Long instanceId, Long trainingRunId) {
    TrainingRun trainingRun = trainingRunService.findById(trainingRunId);
    String sandboxIdentifier =
        trainingRun.getSandboxInstanceRefId() == null
            ? trainingRun.getPreviousSandboxInstanceRefId()
            : trainingRun.getSandboxInstanceRefId();
    return trainingCommandEventsService.findAllConsoleCommandsBySandboxId(sandboxIdentifier);
  }

  /**
   * Gather all the necessary information about the training instance needed to visualize the
   * result.
   *
   * @param trainingInstanceId id of Training Instance to gets info.
   * @return basic info about the training definition of given a training instance and the necessary
   *     info about all levels from that training instance.
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isOrganizerOfGivenTrainingInstance(#trainingInstanceId)")
  @TransactionalRO
  public VisualizationInfoDTO getVisualizationInfoAboutTrainingInstance(Long trainingInstanceId) {
    TrainingInstance trainingInstance = trainingInstanceService.findById(trainingInstanceId);
    TrainingDefinition trainingDefinitionOfTrainingRun = trainingInstance.getTrainingDefinition();
    return new VisualizationInfoDTO(
        trainingDefinitionOfTrainingRun.getId(),
        trainingDefinitionOfTrainingRun.getTitle(),
        trainingDefinitionOfTrainingRun.getEstimatedDuration(),
        convertToAbstractLevelVisualizationDTO(
            visualizationService.getLevelsForOrganizerVisualization(trainingInstance)));
  }

  /**
   * Gather all the necessary information about the users for specific training instance.
   *
   * @param trainingInstanceId id of Training Instance to gets info about all participants.
   * @return basic info about the participants of given a training instance.
   */
  @IsTraineeOrAdmin
  public List<UserRefDTO> getParticipantsForGivenTrainingInstance(Long trainingInstanceId) {
    Set<Long> participantsRefIds =
        visualizationService.getAllParticipantsRefIdsForSpecificTrainingInstance(
            trainingInstanceId);
    return getUserRefsByIds(new ArrayList<>(participantsRefIds)).values().stream().toList();
  }

  /**
   * Gather all the necessary information about the progress of training instance needed to
   * visualize the result.
   *
   * @param trainingInstanceId id of Training Instance to gets info.
   * @return info about the training instance and the necessary info about all players, levels and
   *     events from that training instance.
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isOrganizerOfGivenTrainingInstance(#trainingInstanceId)")
  @TransactionalRO
  public VisualizationProgressDTO getProgressVisualization(Long trainingInstanceId) {
    TrainingInstance trainingInstance = trainingInstanceService.findById(trainingInstanceId);
    TrainingDefinition trainingDefinitionOfTrainingRun = trainingInstance.getTrainingDefinition();

    List<AbstractLevel> abstractLevels =
        trainingDefinitionService.findAllLevelsFromDefinition(
            trainingDefinitionOfTrainingRun.getId());
    Map<Long, String> answerStaticByLevelId =
        abstractLevels.stream()
            .filter(
                abstractLevel ->
                    abstractLevel.getClass() == TrainingLevel.class
                        && ((TrainingLevel) abstractLevel).getAnswer() != null)
            .collect(
                Collectors.toMap(
                    AbstractLevel::getId,
                    abstractLevel -> ((TrainingLevel) abstractLevel).getAnswer()));
    Map<Long, String> answerVariableNameByLevelId =
        abstractLevels.stream()
            .filter(
                abstractLevel ->
                    abstractLevel.getClass() == TrainingLevel.class
                        && ((TrainingLevel) abstractLevel).getAnswerVariableName() != null)
            .collect(
                Collectors.toMap(
                    AbstractLevel::getId,
                    abstractLevel -> ((TrainingLevel) abstractLevel).getAnswerVariableName()));
    Map<String, Map<String, String>> variantAnswersBySandbox =
        getVariantAnswers(trainingInstance, !answerVariableNameByLevelId.isEmpty());

    VisualizationProgressDTO visualizationProgressDTO = new VisualizationProgressDTO();
    visualizationProgressDTO.setStartTime(
        trainingInstance.getStartTime().toEpochSecond(ZoneOffset.UTC));
    visualizationProgressDTO.setCurrentTime(
        LocalDateTime.now(Clock.systemUTC()).toEpochSecond(ZoneOffset.UTC));
    visualizationProgressDTO.setEstimatedEndTime(
        trainingInstance.getEndTime().toEpochSecond(ZoneOffset.UTC));
    visualizationProgressDTO.setLevels(
        getLevelDefinitions(trainingDefinitionOfTrainingRun.getId()));

    Map<Long, Map<Long, List<AbstractAuditPOJO>>> eventsFromOpenSearch =
        trainingCommandEventsService.getAggregatedEventsByTrainingRunsAndLevels(
            trainingInstance.getId());

    // Player progress
    List<ProgressDTO> playerProgresses = new ArrayList<>();
    for (Map.Entry<Long, Map<Long, List<AbstractAuditPOJO>>> userEvents :
        eventsFromOpenSearch.entrySet()) {
      ProgressDTO playerProgress = new ProgressDTO();

      Set<Map.Entry<Long, List<AbstractAuditPOJO>>> allUserEventsByLevelId =
          userEvents.getValue().entrySet();
      for (Map.Entry<Long, List<AbstractAuditPOJO>> levelEvents : allUserEventsByLevelId) {
        List<AbstractAuditPOJO> events = levelEvents.getValue();
        LevelProgressDTO levelProgress = new LevelProgressDTO();
        levelProgress.setLevelId(levelEvents.getKey());
        levelProgress.setState(LevelState.RUNNING);
        // Count wrong answers number
        int levelStartedEventIndex = 0;
        if (events.get(0) instanceof TrainingRunStarted) {
          levelStartedEventIndex = 1;
        }
        levelProgress.setStartTime(events.get(levelStartedEventIndex).getTimestamp());
        if (((LevelStarted) events.get(levelStartedEventIndex)).getLevelType()
            == EventLevelType.TRAINING) {
          this.countWrongAnswersAndAddTakenHints(levelProgress, events);
          levelProgress.setAnswer(
              answerStaticByLevelId.containsKey(levelProgress.getLevelId())
                  ? answerStaticByLevelId.get(levelProgress.getLevelId())
                  : getLevelVariantAnswer(
                      levelProgress.getLevelId(),
                      events.get(0),
                      answerVariableNameByLevelId,
                      variantAnswersBySandbox));
        }

        int levelCompletedEventIndex = getLevelCompletedEventIndex(events);
        if (levelCompletedEventIndex != -1) {
          levelProgress.setState(LevelState.FINISHED);
          levelProgress.setEndTime(events.get(levelCompletedEventIndex).getTimestamp());
        }
        levelProgress.setEvents(events);
        playerProgress.addLevelProgress(levelProgress);

        playerProgress.setTrainingRunId(userEvents.getKey());
        playerProgress.setId(events.get(0).getUserRefId());
      }
      playerProgresses.add(playerProgress);
    }
    playerProgresses = addProgressSubjectData(trainingInstanceId, playerProgresses);
    visualizationProgressDTO.setProgress(playerProgresses);

    return visualizationProgressDTO;
  }

  private List<ProgressDTO> addProgressSubjectData(Long instanceId, List<ProgressDTO> progresses) {
    Map<Long, UserRefDTO> traineesOfInstance =
        this.getParticipantsForGivenTrainingInstance(instanceId).stream()
            .collect(Collectors.toMap(UserRefDTO::getUserRefId, userRefDTO -> userRefDTO));

    for (ProgressDTO progress : progresses) {
      progress.setPicture(traineesOfInstance.get(progress.getId()).getPicture());
      progress.setName(traineesOfInstance.get(progress.getId()).getUserRefFullName());
    }
    return progresses;
  }

  private String getLevelVariantAnswer(
      Long levelId,
      AbstractAuditPOJO levelEvent,
      Map<Long, String> answerVariableNameByLevelId,
      Map<String, Map<String, String>> variantAnswersBySandbox) {
    // if local environment is enabled, the sandbox is identified by user ID, otherwise unique
    // sandbox ID is used
    return variantAnswersBySandbox
        .getOrDefault(levelEvent.getSandboxId(), new HashMap<>())
        .get(answerVariableNameByLevelId.get(levelId));
  }

  private Map<String, Map<String, String>> getVariantAnswers(
      TrainingInstance instance, boolean isVariableNamesDefined) {
    if (!isVariableNamesDefined) {
      return new HashMap<>();
    }
    // TODO cache the response because those data are not changing and frontend requests progress
    // data periodically
    Map<String, Map<String, String>> variantAnswers;
    if (instance.isLocalEnvironment()) {
      variantAnswers =
          answersStorageApiService
              .getAnswersByAccessTokenAndUserIds(
                  instance.getAccessToken(),
                  trainingInstanceService.findAllTraineesByTrainingInstance(instance.getId()))
              .getContent()
              .stream()
              .collect(
                  Collectors.toMap(
                      this::userIdToString,
                      sandboxAnswerInfo ->
                          getAnswersByVariableNames(sandboxAnswerInfo.getVariantAnswers())));
    } else {
      variantAnswers =
          answersStorageApiService
              .getAnswersBySandboxIds(
                  trainingInstanceService.findAllSandboxesUsedByTrainingInstanceId(
                      instance.getId()))
              .getContent()
              .stream()
              .collect(
                  Collectors.toMap(
                      SandboxAnswersInfo::getSandboxRefId,
                      sandboxAnswerInfo ->
                          getAnswersByVariableNames(sandboxAnswerInfo.getVariantAnswers())));
    }
    return variantAnswers;
  }

  private String userIdToString(SandboxAnswersInfo answersInfo) {
    return answersInfo.getUserId().toString();
  }

  private List<LevelDefinitionProgressDTO> getLevelDefinitions(Long definitionId) {
    return trainingRunService.getLevels(definitionId).stream()
        .map(levelMapper::mapToLevelDefinitionProgressDTO)
        .sorted(Comparator.comparingInt(LevelDefinitionProgressDTO::getOrder))
        .toList();
  }

  private Map<String, String> getAnswersByVariableNames(List<VariantAnswer> variantAnswers) {
    return variantAnswers.stream()
        .collect(
            Collectors.toMap(
                VariantAnswer::getAnswerVariableName, VariantAnswer::getAnswerContent));
  }

  private int getLevelCompletedEventIndex(List<AbstractAuditPOJO> events) {
    AbstractAuditPOJO lastEvent = events.get(events.size() - 1);
    if (!(lastEvent instanceof LevelCompleted) && !(lastEvent instanceof TrainingRunFinished)) {
      return -1;
    }
    return lastEvent instanceof TrainingRunFinished ? events.size() - 2 : events.size() - 1;
  }

  private void countWrongAnswersAndAddTakenHints(
      LevelProgressDTO levelProgress, List<AbstractAuditPOJO> events) {
    levelProgress.setWrongAnswersNumber(0L);
    for (AbstractAuditPOJO event : events) {
      if (event instanceof WrongAnswerSubmitted) {
        levelProgress.increaseWrongAnswersNumber();
      } else if (event instanceof HintTaken) {
        levelProgress.addHintTaken(((HintTaken) event).getHintId());
      }
    }
  }

  private List<AbstractLevelVisualizationDTO> convertToAbstractLevelVisualizationDTO(
      List<AbstractLevel> abstractLevels) {
    List<AbstractLevelVisualizationDTO> visualizationLevelInfoDTOs = new ArrayList<>();
    abstractLevels.forEach(
        level -> visualizationLevelInfoDTOs.add(levelMapper.mapToVisualizationDTO(level)));
    return visualizationLevelInfoDTOs;
  }

  /**
   * Gather all the necessary information for organizer to display clustering visualizations.
   *
   * @param trainingInstanceId id of training instance.
   * @return data for clustering visualizations.
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isOrganizerOfGivenTrainingInstance(#trainingInstanceId)")
  @TransactionalRO
  public ClusteringVisualizationDTO getClusteringVisualizationsForOrganizer(
      Long trainingInstanceId) {
    return getClusteringVisualizations(trainingInstanceId);
  }

  /**
   * Gather all the necessary information for designer to display clustering visualizations.
   *
   * @param trainingDefinitionId id of training definition
   * @return {@link ClusteringVisualizationDTO} with data from all training instances with the
   *     specified training definition
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isDesignerOfGivenTrainingDefinition(#trainingDefinitionId)")
  @TransactionalRO
  public ClusteringVisualizationDTO getClusteringVisualizationsForTrainingDefinition(
      Long trainingDefinitionId) {
    List<TrainingInstance> instances =
        trainingDefinitionService.findAllTrainingInstancesByTrainingDefinitionId(
            trainingDefinitionId);
    TrainingDefinition trainingDefinition =
        trainingDefinitionService.findById(trainingDefinitionId);
    Long estimatedDuration = trainingDefinition.getEstimatedDuration();
    List<AbstractLevel> levels =
        trainingDefinitionService.findAllLevelsFromDefinition(trainingDefinition.getId());
    return getClustering(instances, levels, estimatedDuration);
  }

  /**
   * Gather all the necessary information for designer to display clustering visualizations from
   * specified instances
   *
   * @param instanceIds ids of training instances
   * @return {@link ClusteringVisualizationDTO} with data from all the specified training instances
   */
  @TransactionalRO
  public ClusteringVisualizationDTO getClusteringForTrainingInstances(List<Long> instanceIds) {
    List<TrainingInstance> instances =
        instanceIds.stream().map(trainingInstanceService::findById).toList();
    TrainingDefinition trainingDefinition =
        trainingInstanceService
            .findByIdIncludingDefinition(instanceIds.get(0))
            .getTrainingDefinition();
    Long estimatedDuration = trainingDefinition.getEstimatedDuration();
    List<AbstractLevel> levels =
        trainingDefinitionService.findAllLevelsFromDefinition(trainingDefinition.getId());
    return getClustering(instances, levels, estimatedDuration);
  }

  private ClusteringVisualizationDTO getClustering(
      List<TrainingInstance> instances, List<AbstractLevel> levels, Long estimatedDuration) {
    TrainingInstanceStatistics trainingInstanceStatistics = new TrainingInstanceStatistics();
    TrainingData trainingData =
        getTrainingData(instances, levels, estimatedDuration, trainingInstanceStatistics);

    int maxScore = 0;
    for (AbstractLevel level : levels) {
      maxScore += level.getMaxScore();
    }
    // we reuse the TrainingInstanceStatistics class as the corresponding class
    // for training definition statistics would have the same fields and same functions
    TrainingResultsDTO finalResultsField =
        mapToFinalResultsDTO(
            trainingData.estimatedDuration,
            trainingData.participantsByTrainingRuns,
            trainingInstanceStatistics);
    finalResultsField.setMaxAchievableScore(maxScore);
    return new ClusteringVisualizationDTO(finalResultsField, trainingData.levelsField);
  }

  private TrainingData getTrainingData(
      List<TrainingInstance> instances,
      List<AbstractLevel> levels,
      Long estimatedDuration,
      TrainingInstanceStatistics trainingInstanceStatistics) {
    TrainingData trainingData = new TrainingData();
    trainingData.estimatedDuration = estimatedDuration;
    trainingData.participantsByTrainingRuns = new HashMap<>();
    trainingData.levelsField = new ArrayList<>(levels.size());
    // the mapping is map<levelId, map<runId, list<events>>>
    trainingData.events = new HashMap<>(levels.size());

    for (TrainingInstance trainingInstance : instances) {
      Map<Long, Map<Long, List<AbstractAuditPOJO>>> instanceEvents =
          trainingCommandEventsService.getAggregatedEventsByLevelsAndTrainingRuns(
              trainingInstance.getId());
      for (AbstractLevel level : levels) {
        Map<Long, List<AbstractAuditPOJO>> levelEvents = instanceEvents.get(level.getId());
        if (levelEvents != null) {
          trainingData
              .events
              .computeIfAbsent(level.getId(), value -> new HashMap<>())
              .putAll(levelEvents);
        }
      }

      trainingData.participantsByTrainingRuns.putAll(
          getUserRefDTOsFromInstanceEvents(
              trainingInstance.getId(),
              instanceEvents,
              this::retrieveRunsIdsFromEventsAggregatedByLevelsAndTrainingRuns));
    }

    for (AbstractLevel level : levels) {
      trainingData.levelsField.add(
          mapToLevelResultDTO(level, trainingData, trainingInstanceStatistics));
    }
    return trainingData;
  }

  /**
   * Gather all the necessary information for trainee to display clustering visualizations.
   *
   * @param trainingRunId id of training run.
   * @return data for clustering visualizations.
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isTraineeOfGivenTrainingRun(#trainingRunId)")
  @TransactionalWO
  public ClusteringVisualizationDTO getClusteringVisualizationsForTrainee(Long trainingRunId) {
    TrainingRun trainingRun = trainingRunService.findByIdWithLevel(trainingRunId);
    return getClusteringVisualizations(trainingRun.getTrainingInstance().getId());
  }

  private ClusteringVisualizationDTO getClusteringVisualizations(Long trainingInstanceId) {
    TrainingInstanceData trainingInstanceData =
        getTrainingInstanceData(
            trainingInstanceId,
            trainingCommandEventsService::getAggregatedEventsByLevelsAndTrainingRuns,
            this::retrieveRunsIdsFromEventsAggregatedByLevelsAndTrainingRuns);
    TrainingInstanceStatistics trainingInstanceStatistics = new TrainingInstanceStatistics();
    TrainingData trainingData =
        new TrainingData(
            trainingInstanceData.trainingDefinition.getEstimatedDuration(),
            trainingInstanceData.events,
            trainingInstanceData.participantsByTrainingRuns);
    // Must be before getFinalResultsField() because of statistics
    List<ClusteringLevelDTO> levelsField = new ArrayList<>();
    int maxScore = 0;
    for (AbstractLevel abstractLevel : trainingInstanceData.levels) {
      levelsField.add(mapToLevelResultDTO(abstractLevel, trainingData, trainingInstanceStatistics));
      maxScore += abstractLevel.getMaxScore();
    }
    TrainingResultsDTO finalResultsField =
        mapToFinalResultsDTO(
            trainingData.estimatedDuration,
            trainingData.participantsByTrainingRuns,
            trainingInstanceStatistics);
    finalResultsField.setMaxAchievableScore(maxScore);
    return new ClusteringVisualizationDTO(finalResultsField, levelsField);
  }

  /**
   * Gather all the necessary information for organizer to display table visualizations.
   *
   * @param trainingInstanceId id of training instance.
   * @return data for table visualizations.
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isOrganizerOfGivenTrainingInstance(#trainingInstanceId)")
  @TransactionalRO
  public List<PlayerDataDTO> getTableVisualizationsForOrganizer(Long trainingInstanceId) {
    return getTableVisualizations(trainingInstanceId);
  }

  /**
   * Gather all the necessary information for trainee to display table visualizations.
   *
   * @param trainingRunId id of training run.
   * @return data for table visualizations.
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isTraineeOfGivenTrainingRun(#trainingRunId)")
  @TransactionalWO
  public List<PlayerDataDTO> getTableVisualizationsForTrainee(Long trainingRunId) {
    TrainingRun trainingRun = trainingRunService.findByIdWithLevel(trainingRunId);
    return getTableVisualizations(trainingRun.getTrainingInstance().getId());
  }

  private List<PlayerDataDTO> getTableVisualizations(Long trainingInstanceId) {
    TrainingInstanceData trainingInstanceData =
        getTrainingInstanceData(
            trainingInstanceId,
            trainingCommandEventsService::getAggregatedEventsByTrainingRunsAndLevels,
            this::retrieveRunIdsFromEventsAggregatedByRunsAndLevels);

    return trainingInstanceData.events.entrySet().stream()
        .map(
            runEvents -> {
              AbstractAuditPOJO lastLevelEvent = null;
              UserRefDTO participantInfo =
                  trainingInstanceData.participantsByTrainingRuns.get(runEvents.getKey());
              TablePlayerDTO tablePlayerDataDTO =
                  new TablePlayerDTO(participantInfo, runEvents.getKey());

              for (AbstractLevel abstractLevel : trainingInstanceData.levels) {
                List<AbstractAuditPOJO> levelEvents =
                    runEvents.getValue().get(abstractLevel.getId());
                if (levelEvents != null) {
                  lastLevelEvent = levelEvents.get(levelEvents.size() - 1);
                } else {
                  levelEvents = Collections.emptyList();
                }
                tablePlayerDataDTO.addTableLevel(mapToTableLevelDTO(abstractLevel, levelEvents));
              }
              tablePlayerDataDTO.setTrainingScore(
                  lastLevelEvent == null ? 0 : lastLevelEvent.getTotalTrainingScore());
              tablePlayerDataDTO.setAssessmentScore(
                  lastLevelEvent == null ? 0 : lastLevelEvent.getTotalAssessmentScore());
              tablePlayerDataDTO.setFinished(lastLevelEvent instanceof TrainingRunFinished);
              tablePlayerDataDTO.setTrainingTime(
                  lastLevelEvent == null ? 0 : lastLevelEvent.getTrainingTime());
              return tablePlayerDataDTO;
            })
        .collect(Collectors.toList());
  }

  /**
   * Gather all the necessary information about the training instance to display table
   * visualizations.
   *
   * @param trainingInstanceId id of Training Instance to gets info.
   * @return data for table visualizations.
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isOrganizerOfGivenTrainingInstance(#trainingInstanceId)")
  @TransactionalRO
  public List<LevelTabsLevelDTO> getLevelTabsVisualizations(Long trainingInstanceId) {
    TrainingInstanceData trainingInstanceData =
        getTrainingInstanceData(
            trainingInstanceId,
            trainingCommandEventsService::getAggregatedEventsByLevelsAndTrainingRuns,
            this::retrieveRunsIdsFromEventsAggregatedByLevelsAndTrainingRuns);
    List<LevelTabsLevelDTO> levelTabsData = new ArrayList<>();
    for (AbstractLevel level : trainingInstanceData.levels) {
      levelTabsData.add(mapToLevelTabsLevelDTO(level, trainingInstanceData));
    }
    return levelTabsData;
  }

  /**
   * Gather all summarized data about mitre techniques used in training definitions.
   *
   * @return training definitions with mitre techniques
   */
  @IsTrainee
  @TransactionalRO
  public List<TrainingDefinitionMitreTechniquesDTO> getTrainingDefinitionsWithMitreTechniques() {
    List<TrainingDefinition> trainingDefinitions =
        trainingDefinitionService
            .findAllByState(TDState.RELEASED, PageRequest.of(0, Integer.MAX_VALUE))
            .getContent();
    List<TrainingLevel> trainingLevels = visualizationService.getAllTrainingLevels();
    UserRefDTO userRefDTO = userService.getUserRefFromUserAndGroup();
    Set<Long> playedDefinitionIds =
        trainingDefinitionService.findAllPlayedByUser(userRefDTO.getUserRefId()).stream()
            .map(TrainingDefinition::getId)
            .collect(Collectors.toSet());

    List<TrainingDefinitionMitreTechniquesDTO> result = new ArrayList<>();

    for (TrainingDefinition trainingDefinition : trainingDefinitions) {
      List<TrainingLevel> trainingLevelsOfDefinition =
          visualizationService.getTrainingLevelsByTrainingDefinitionId(trainingDefinition.getId());
      TrainingDefinitionMitreTechniquesDTO definitionMitreTechniquesDTO =
          new TrainingDefinitionMitreTechniquesDTO();
      definitionMitreTechniquesDTO.setId(trainingDefinition.getId());
      definitionMitreTechniquesDTO.setTitle(trainingDefinition.getTitle());
      List<String> techniques =
          trainingLevelsOfDefinition.stream()
              .flatMap(
                  trainingLevel ->
                      trainingLevel.getMitreTechniques().stream()
                          .map(MitreTechnique::getTechniqueKey))
              .collect(Collectors.toList());
      definitionMitreTechniquesDTO.setMitreTechniques(techniques);
      definitionMitreTechniquesDTO.setPlayed(
          playedDefinitionIds.contains(trainingDefinition.getId()));
      result.add(definitionMitreTechniquesDTO);
    }
    return result;
  }

  /**
   * Gather all the necessary information for organizer to display timeline visualizations.
   *
   * @param trainingInstanceId id of Training Instance to gets info.
   * @return data for clustering visualizations.
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isOrganizerOfGivenTrainingInstance(#trainingInstanceId)")
  @TransactionalRO
  public TimelineDTO getTimelineVisualizationsForTrainingInstance(Long trainingInstanceId) {
    return getTimelineVisualizations(trainingInstanceId);
  }

  /**
   * Gather all the necessary information for trainee to display timeline visualizations.
   *
   * @param trainingRunId id of training run.
   * @return data for clustering visualizations.
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isTraineeOfGivenTrainingRun(#trainingRunId)")
  @TransactionalWO
  public TimelineDTO getTimelineVisualizationsForTrainee(Long trainingRunId) {
    TrainingRun trainingRun = trainingRunService.findByIdWithLevel(trainingRunId);
    return getTimelineVisualizations(trainingRun.getTrainingInstance().getId());
  }

  private TimelineDTO getTimelineVisualizations(Long trainingInstanceId) {
    TrainingInstanceData trainingInstanceData =
        getTrainingInstanceData(
            trainingInstanceId,
            trainingCommandEventsService::getAggregatedEventsByTrainingRunsAndLevels,
            this::retrieveRunIdsFromEventsAggregatedByRunsAndLevels);

    TimelineDTO timelineDTO = new TimelineDTO();
    timelineDTO.setEstimatedTime(
        TimeUnit.MINUTES.toMillis(trainingInstanceData.trainingDefinition.getEstimatedDuration()));
    timelineDTO.setMaxScoreOfLevels(
        trainingInstanceData.levels.stream()
            .map(AbstractLevel::getMaxScore)
            .collect(Collectors.toList()));

    TrainingInstanceStatistics trainingInstanceStatistics = new TrainingInstanceStatistics();
    for (Map.Entry<Long, Map<Long, List<AbstractAuditPOJO>>> runEvents :
        trainingInstanceData.events.entrySet()) {
      timelineDTO.addPlayerData(
          mapToTimelinePlayerDataDTO(
              trainingInstanceData.participantsByTrainingRuns.get(runEvents.getKey()),
              trainingInstanceData.levels,
              runEvents,
              trainingInstanceStatistics));
    }
    timelineDTO.setAverageTime(trainingInstanceStatistics.getAverageTime());
    timelineDTO.setMaxParticipantTime(trainingInstanceStatistics.getMaxTime());
    return timelineDTO;
  }

  // Private methods
  private PlayerDataDTO mapToTimelinePlayerDataDTO(
      UserRefDTO player,
      List<AbstractLevel> levels,
      Map.Entry<Long, Map<Long, List<AbstractAuditPOJO>>> runEvents,
      TrainingInstanceStatistics trainingInstanceStatistics) {
    AbstractAuditPOJO firstEvent = runEvents.getValue().get(levels.get(0).getId()).get(0);
    ProcessedLevelsData processedLevelsData = getProcessedLevelsData(levels, runEvents);
    trainingInstanceStatistics.addTrainingRun(processedLevelsData.lastLevelEvent);
    int trainingScore = processedLevelsData.lastLevelEvent.getTotalTrainingScore();
    int assessmentScore = processedLevelsData.lastLevelEvent.getTotalAssessmentScore();

    TimelinePlayerDTO timelinePlayerDTO =
        new TimelinePlayerDTO(
            player, firstEvent.getTrainingRunId(), trainingScore, assessmentScore);
    timelinePlayerDTO.setTrainingTime(
        processedLevelsData.lastLevelEvent.getTrainingTime() - firstEvent.getTrainingTime());
    timelinePlayerDTO.setLevels(processedLevelsData.timelineLevels);
    return timelinePlayerDTO;
  }

  private LevelTabsLevelDTO mapToLevelTabsLevelDTO(
      AbstractLevel level, TrainingInstanceData trainingInstanceData) {
    LevelTabsLevelDTO.LevelTabsLevelBuilder levelTabsLevelBuilder =
        new LevelTabsLevelDTO.LevelTabsLevelBuilder()
            .id(level.getId())
            .title(level.getTitle())
            .estimatedTime(TimeUnit.MINUTES.toMillis(level.getEstimatedDuration()))
            .order(level.getOrder())
            .maxLevelScore(level.getMaxScore());
    if (level instanceof TrainingLevel) {
      levelTabsLevelBuilder
          .levelType(cz.cyberrange.platform.training.api.enums.LevelType.TRAINING_LEVEL)
          .content(((TrainingLevel) level).getContent())
          .correctAnswer(((TrainingLevel) level).getAnswer())
          .players(mapToLevelTabsPlayerDTOs(trainingInstanceData.events, level))
          .hints(
              ((TrainingLevel) level)
                  .getHints().stream()
                      .map(
                          hint ->
                              new LevelTabsHintDTO(
                                  hint.getId(),
                                  hint.getOrder(),
                                  hint.getTitle(),
                                  hint.getHintPenalty()))
                      .collect(Collectors.toList()));
    } else if (level instanceof InfoLevel) {
      levelTabsLevelBuilder
          .levelType(cz.cyberrange.platform.training.api.enums.LevelType.INFO_LEVEL)
          .content(((InfoLevel) level).getContent());
    } else if (level instanceof AccessLevel) {
      levelTabsLevelBuilder
          .levelType(cz.cyberrange.platform.training.api.enums.LevelType.ACCESS_LEVEL)
          .content(
              trainingInstanceData.trainingInstance.isLocalEnvironment()
                  ? ((AccessLevel) level).getLocalContent()
                  : ((AccessLevel) level).getCloudContent());
    } else {
      levelTabsLevelBuilder
          .levelType(cz.cyberrange.platform.training.api.enums.LevelType.ASSESSMENT_LEVEL)
          .assessmentType(
              AssessmentType.valueOf(((AssessmentLevel) level).getAssessmentType().name()));
    }
    return levelTabsLevelBuilder.build();
  }

  private List<LevelTabsPlayerDTO> mapToLevelTabsPlayerDTOs(
      Map<Long, Map<Long, List<AbstractAuditPOJO>>> events, AbstractLevel level) {
    List<LevelTabsPlayerDTO> players = new ArrayList<>();
    for (Map.Entry<Long, List<AbstractAuditPOJO>> runEvents :
        events.getOrDefault(level.getId(), Collections.emptyMap()).entrySet()) {
      AbstractAuditPOJO firstEvent = runEvents.getValue().get(0);
      AbstractAuditPOJO lastEvent = runEvents.getValue().get(runEvents.getValue().size() - 1);

      LevelTabsPlayerDTO levelTabPlayerDTO = new LevelTabsPlayerDTO();
      levelTabPlayerDTO.setId(firstEvent.getUserRefId());
      levelTabPlayerDTO.setTrainingRunId(firstEvent.getTrainingRunId());
      levelTabPlayerDTO.setDisplayedSolution(false);
      levelTabPlayerDTO.setTime(lastEvent.getTrainingTime() - firstEvent.getTrainingTime());
      levelTabPlayerDTO.setParticipantLevelScore(lastEvent.getActualScoreInLevel());
      for (AbstractAuditPOJO userLevelEvent : runEvents.getValue()) {
        if (userLevelEvent instanceof SolutionDisplayed) {
          levelTabPlayerDTO.setDisplayedSolution(true);
        } else if (userLevelEvent instanceof HintTaken) {
          levelTabPlayerDTO.addHint();
        } else if (userLevelEvent instanceof WrongAnswerSubmitted) {
          levelTabPlayerDTO.addWrongAnswer(
              ((WrongAnswerSubmitted) userLevelEvent).getAnswerContent());
        }
      }
      players.add(levelTabPlayerDTO);
    }
    return players;
  }

  private TableLevelDTO mapToTableLevelDTO(
      AbstractLevel abstractLevel, List<AbstractAuditPOJO> levelEvents) {
    TableLevelDTO.TableLevelBuilder tableLevelBuilder =
        new TableLevelDTO.TableLevelBuilder()
            .id(abstractLevel.getId())
            .order(abstractLevel.getOrder());
    AbstractAuditPOJO lastLevelEvent =
        levelEvents.isEmpty() ? null : levelEvents.get(levelEvents.size() - 1);
    if (!levelEvents.isEmpty()
        && (lastLevelEvent instanceof LevelCompleted
            || lastLevelEvent instanceof TrainingRunFinished)) {
      tableLevelBuilder.score(lastLevelEvent.getActualScoreInLevel());
    }

    if (abstractLevel instanceof TrainingLevel) {
      tableLevelBuilder.levelType(
          cz.cyberrange.platform.training.api.enums.LevelType.TRAINING_LEVEL);
      countHintsTakenAndWrongAnswers(tableLevelBuilder, levelEvents);
    } else if (abstractLevel instanceof AssessmentLevel) {
      tableLevelBuilder.levelType(
          cz.cyberrange.platform.training.api.enums.LevelType.ASSESSMENT_LEVEL);
    } else if (abstractLevel instanceof InfoLevel) {
      tableLevelBuilder.levelType(cz.cyberrange.platform.training.api.enums.LevelType.INFO_LEVEL);
    } else if (abstractLevel instanceof AccessLevel) {
      tableLevelBuilder.levelType(cz.cyberrange.platform.training.api.enums.LevelType.ACCESS_LEVEL);
    }
    return tableLevelBuilder.build();
  }

  private ClusteringLevelDTO mapToLevelResultDTO(
      AbstractLevel abstractLevel,
      TrainingData trainingData,
      TrainingInstanceStatistics trainingInstanceStatistics) {
    ClusteringLevelDTO.ClusteringLevelBuilder clusteringLevelBuilder =
        new ClusteringLevelDTO.ClusteringLevelBuilder()
            .id(abstractLevel.getId())
            .title(abstractLevel.getTitle())
            .order(abstractLevel.getOrder())
            .estimatedTime(TimeUnit.MINUTES.toMillis(abstractLevel.getEstimatedDuration()))
            .maxAchievableScore(abstractLevel.getMaxScore());
    if (abstractLevel instanceof TrainingLevel) {
      clusteringLevelBuilder.levelType(
          cz.cyberrange.platform.training.api.enums.LevelType.TRAINING_LEVEL);
    } else if (abstractLevel instanceof InfoLevel) {
      clusteringLevelBuilder.levelType(
          cz.cyberrange.platform.training.api.enums.LevelType.INFO_LEVEL);
    } else if (abstractLevel instanceof AssessmentLevel) {
      clusteringLevelBuilder.levelType(
          cz.cyberrange.platform.training.api.enums.LevelType.ASSESSMENT_LEVEL);
    } else if (abstractLevel instanceof AccessLevel) {
      clusteringLevelBuilder.levelType(
          cz.cyberrange.platform.training.api.enums.LevelType.ACCESS_LEVEL);
    }
    LevelStatistics levelStatistics = new LevelStatistics();
    Map<Long, List<AbstractAuditPOJO>> levelEventsOfTrainingRuns =
        trainingData.events.getOrDefault(abstractLevel.getId(), Collections.emptyMap());
    for (Map.Entry<Long, List<AbstractAuditPOJO>> trainingRunLevelEvents :
        levelEventsOfTrainingRuns.entrySet()) {
      AbstractAuditPOJO firstLevelEvent = trainingRunLevelEvents.getValue().get(0);
      AbstractAuditPOJO lastLevelEvent =
          trainingRunLevelEvents.getValue().get(trainingRunLevelEvents.getValue().size() - 1);
      UserRefDTO participantInfo =
          trainingData.participantsByTrainingRuns.get(trainingRunLevelEvents.getKey());
      ClusteringLevelPlayerDTO playerDataDTOForLevel =
          new ClusteringLevelPlayerDTO(
              participantInfo,
              trainingRunLevelEvents.getKey(),
              lastLevelEvent.getTrainingTime() - firstLevelEvent.getTrainingTime(),
              lastLevelEvent.getActualScoreInLevel(),
              lastLevelEvent instanceof LevelCompleted);
      levelStatistics.updateStatistics(
          playerDataDTOForLevel.getTrainingTime(),
          playerDataDTOForLevel.getParticipantLevelScore());
      if (!(lastLevelEvent instanceof LevelCompleted)) {
        trainingInstanceStatistics.addTrainingRun(lastLevelEvent);
      }
      trainingInstanceStatistics.lastEventsOfTrainingRuns.put(
          playerDataDTOForLevel.getTrainingRunId(), lastLevelEvent);
      clusteringLevelBuilder.addPlayerData(playerDataDTOForLevel);
    }
    clusteringLevelBuilder
        .averageScore(levelStatistics.getAverageScore())
        .averageTime(levelStatistics.getAverageTime())
        .maxParticipantScore(levelStatistics.getMaxScore())
        .maxParticipantTime(levelStatistics.getMaxTime());
    return clusteringLevelBuilder.build();
  }

  private void countHintsTakenAndWrongAnswers(
      TableLevelDTO.TableLevelBuilder builder, List<AbstractAuditPOJO> levelEvents) {
    int wrongAnswers = 0;
    int hintsTaken = 0;
    for (AbstractAuditPOJO levelEvent : levelEvents) {
      if (levelEvent instanceof WrongAnswerSubmitted) {
        wrongAnswers++;
      }
      if (levelEvent instanceof HintTaken) {
        hintsTaken++;
      }
    }
    builder.hintsTaken(hintsTaken).wrongAnswers(wrongAnswers);
  }

  private TrainingResultsDTO mapToFinalResultsDTO(
      Long estimatedDuration,
      Map<Long, UserRefDTO> participants,
      TrainingInstanceStatistics trainingInstanceStatistics) {
    TrainingResultsDTO finalResults = new TrainingResultsDTO();
    finalResults.setEstimatedTime(TimeUnit.MINUTES.toMillis(estimatedDuration));
    for (Map.Entry<Long, AbstractAuditPOJO> lastEventOfTrainingRun :
        trainingInstanceStatistics.lastEventsOfTrainingRuns.entrySet()) {
      UserRefDTO participantInfo = participants.get(lastEventOfTrainingRun.getKey());
      finalResults.addPlayerData(
          new TrainingResultsPlayerDTO(
              participantInfo,
              lastEventOfTrainingRun.getKey(),
              lastEventOfTrainingRun.getValue().getTrainingTime(),
              lastEventOfTrainingRun.getValue().getTotalTrainingScore(),
              lastEventOfTrainingRun.getValue().getTotalAssessmentScore(),
              lastEventOfTrainingRun.getValue() instanceof TrainingRunFinished));
    }
    finalResults.setAverageScore(trainingInstanceStatistics.getAverageScore());
    finalResults.setAverageTrainingScore(trainingInstanceStatistics.getAverageTrainingScore());
    finalResults.setAverageAssessmentScore(trainingInstanceStatistics.getAverageAssessmentScore());
    finalResults.setMaxParticipantScore(trainingInstanceStatistics.getMaxScore());
    finalResults.setMaxParticipantTrainingScore(trainingInstanceStatistics.getMaxTrainingScore());
    finalResults.setMaxParticipantAssessmentScore(
        trainingInstanceStatistics.getMaxAssessmentScore());
    finalResults.setAverageTime(trainingInstanceStatistics.getAverageTime());
    finalResults.setMaxParticipantTime(trainingInstanceStatistics.getMaxTime());
    return finalResults;
  }

  private TrainingInstanceData getTrainingInstanceData(
      Long trainingInstanceId,
      Function<Long, Map<Long, Map<Long, List<AbstractAuditPOJO>>>> aggregatedEventsFunction,
      Function<Map<Long, Map<Long, List<AbstractAuditPOJO>>>, Set<Long>>
          trainingRunsIdsRetrieveFunction) {
    TrainingInstanceData trainingInstanceData = new TrainingInstanceData();
    trainingInstanceData.trainingInstance = trainingInstanceService.findById(trainingInstanceId);
    trainingInstanceData.trainingDefinition =
        trainingInstanceData.trainingInstance.getTrainingDefinition();
    trainingInstanceData.levels =
        trainingDefinitionService.findAllLevelsFromDefinition(
            trainingInstanceData.trainingInstance.getTrainingDefinition().getId());
    trainingInstanceData.events =
        aggregatedEventsFunction.apply(trainingInstanceData.trainingInstance.getId());
    trainingInstanceData.participantsByTrainingRuns =
        getUserRefDTOsFromInstanceEvents(
            trainingInstanceId, trainingInstanceData.events, trainingRunsIdsRetrieveFunction);
    return trainingInstanceData;
  }

  private Map<Long, UserRefDTO> getUserRefDTOsFromInstanceEvents(
      Long trainingInstanceId,
      Map<Long, Map<Long, List<AbstractAuditPOJO>>> trainingInstanceEvents,
      Function<Map<Long, Map<Long, List<AbstractAuditPOJO>>>, Set<Long>>
          trainingRunsIdsRetrieveFunction) {
    Set<Long> trainingRunsIdsFromEvents =
        trainingRunsIdsRetrieveFunction.apply(trainingInstanceEvents);
    Set<TrainingRun> foundRuns =
        trainingRunService.findAllByTrainingInstanceId(trainingInstanceId).stream()
            .filter(trainingRun -> trainingRunsIdsFromEvents.contains(trainingRun.getId()))
            .collect(Collectors.toSet());
    List<Long> userRefIds =
        foundRuns.stream()
            .map(trainingRun -> trainingRun.getParticipantRef().getUserRefId())
            .toList();

    Map<Long, UserRefDTO> userRefDTOsById = getUserRefsByIds(userRefIds);
    return foundRuns.stream()
        .collect(
            Collectors.toMap(
                TrainingRun::getId,
                trainingRun ->
                    userRefDTOsById.get(trainingRun.getParticipantRef().getUserRefId())));
  }

  private Map<Long, UserRefDTO> getUserRefsByIds(List<Long> userRefIds) {
    Map<Long, UserRefDTO> users = new HashMap<>(userRefIds.size());
    PageResultResource<UserRefDTO> participantsInfo;
    int page = 0;

    do {
      participantsInfo =
          userService.getUsersRefDTOByGivenUserIds(
              userRefIds, PageRequest.of(page, 999), null, null);
      participantsInfo
          .getContent()
          .forEach(userRefDTO -> users.put(userRefDTO.getUserRefId(), userRefDTO));
      page++;
    } while (page < participantsInfo.getPagination().getTotalPages());

    return users;
  }

  private Set<Long> retrieveRunsIdsFromEventsAggregatedByLevelsAndTrainingRuns(
      Map<Long, Map<Long, List<AbstractAuditPOJO>>> events) {
    return events.values().stream().flatMap(v -> v.keySet().stream()).collect(Collectors.toSet());
  }

  private Set<Long> retrieveRunIdsFromEventsAggregatedByRunsAndLevels(
      Map<Long, Map<Long, List<AbstractAuditPOJO>>> events) {
    return events.keySet();
  }

  private ProcessedLevelsData getProcessedLevelsData(
      List<AbstractLevel> levels, Map.Entry<Long, Map<Long, List<AbstractAuditPOJO>>> userEvents) {
    ProcessedLevelsData processedLevelData = new ProcessedLevelsData();
    for (AbstractLevel abstractLevel : levels) {
      List<AbstractAuditPOJO> levelEvents = userEvents.getValue().get(abstractLevel.getId());
      if (levelEvents == null) {
        continue;
      }
      ProcessedEventsData processedEventsData =
          getProcessedEventsData(abstractLevel.getOrder(), levelEvents);
      TimelineLevelDTO.TimelineLevelBuilder timelineLevelBuilder =
          new TimelineLevelDTO.TimelineLevelBuilder()
              .id(abstractLevel.getId())
              .order(abstractLevel.getOrder())
              .startTime(levelEvents.get(0).getTrainingTime())
              .events(processedEventsData.timelineEvents)
              .participantScore(processedEventsData.lastLevelEvent.getActualScoreInLevel());

      if (abstractLevel instanceof TrainingLevel) {
        timelineLevelBuilder
            .levelType(cz.cyberrange.platform.training.api.enums.LevelType.TRAINING_LEVEL)
            .order(abstractLevel.getOrder())
            .correctAnswerTime(processedEventsData.correctAnswerTime)
            .solutionDisplayedTime(processedEventsData.solutionDisplayedTime);
      } else if (abstractLevel instanceof InfoLevel) {
        timelineLevelBuilder.levelType(
            cz.cyberrange.platform.training.api.enums.LevelType.INFO_LEVEL);
      } else if (abstractLevel instanceof AccessLevel) {
        timelineLevelBuilder.levelType(
            cz.cyberrange.platform.training.api.enums.LevelType.ACCESS_LEVEL);
      } else {
        timelineLevelBuilder
            .levelType(cz.cyberrange.platform.training.api.enums.LevelType.ASSESSMENT_LEVEL)
            .assessmentType(
                AssessmentType.valueOf(
                    ((AssessmentLevel) abstractLevel).getAssessmentType().name()));
      }
      processedLevelData.lastLevelEvent = processedEventsData.lastLevelEvent;
      processedLevelData.timelineLevels.add(timelineLevelBuilder.build());
    }
    return processedLevelData;
  }

  private ProcessedEventsData getProcessedEventsData(
      int levelOrder, List<AbstractAuditPOJO> levelEvents) {
    ProcessedEventsData processedEventsData = new ProcessedEventsData();
    for (AbstractAuditPOJO levelEvent : levelEvents) {
      int score =
          levelEvent.getTotalTrainingScore()
              + levelEvent.getTotalAssessmentScore()
              + levelEvent.getActualScoreInLevel();
      EventDTO eventDTO = new EventDTO(levelEvent.getTrainingTime());
      if (levelEvent instanceof TrainingRunStarted) {
        eventDTO.setText("Training run " + levelEvent.getTrainingRunId() + " started.");
      } else if (levelEvent instanceof LevelStarted) {
        eventDTO.setText("Level " + levelOrder + " started.");
      } else if (levelEvent instanceof HintTaken) {
        eventDTO.setText("Hint '" + ((HintTaken) levelEvent).getHintTitle() + "' taken.");
      } else if (levelEvent instanceof WrongAnswerSubmitted) {
        eventDTO.setText("Wrong answer submitted.");
      } else if (levelEvent instanceof CorrectAnswerSubmitted) {
        score -= levelEvent.getActualScoreInLevel();
        processedEventsData.correctAnswerTime = levelEvent.getTrainingTime();
        eventDTO.setText("Correct answer submitted.");
      } else if (levelEvent instanceof SolutionDisplayed) {
        score -= levelEvent.getActualScoreInLevel();
        eventDTO.setText("Solution displayed.");
        processedEventsData.solutionDisplayedTime = levelEvent.getTrainingTime();
      } else if (levelEvent instanceof LevelCompleted) {
        score -= levelEvent.getActualScoreInLevel();
        eventDTO.setText("Level " + levelOrder + " completed.");
      } else if (levelEvent instanceof TrainingRunFinished) {
        score -= levelEvent.getActualScoreInLevel();
        eventDTO.setText("Training run " + levelEvent.getTrainingRunId() + " ended.");
      } else if (levelEvent instanceof TrainingRunResumed) {
        eventDTO.setText("Training run " + levelEvent.getTrainingRunId() + " resumed.");
      } else if (levelEvent instanceof AssessmentAnswered) {
        score -= levelEvent.getActualScoreInLevel();
        eventDTO.setText("Assessment answered.");
      }
      eventDTO.setScore(score);
      processedEventsData.timelineEvents.add(eventDTO);
      processedEventsData.lastLevelEvent = levelEvent;
    }
    return processedEventsData;
  }

  // Inner classes
  private class TrainingInstanceStatistics {
    float sumOfTrainingRunsTime;
    float sumOfTrainingRunsTrainingScore;
    float sumOfTrainingRunsAssessmentScore;
    int numOfFinishedTrainingRuns;
    long maxTime;
    int maxScore;
    int maxTrainingScore;
    int maxAssessmentScore;
    Map<Long, AbstractAuditPOJO> lastEventsOfTrainingRuns = new HashMap<>();

    public void addTrainingRun(AbstractAuditPOJO lastLevelEvent) {
      this.sumOfTrainingRunsTime += lastLevelEvent.getTrainingTime();
      this.sumOfTrainingRunsTrainingScore += lastLevelEvent.getTotalTrainingScore();
      this.sumOfTrainingRunsAssessmentScore += lastLevelEvent.getTotalAssessmentScore();
      this.checkAndSetMaxTime(lastLevelEvent.getTrainingTime());
      this.checkAndSetMaxScore(
          lastLevelEvent.getTotalTrainingScore() + lastLevelEvent.getTotalAssessmentScore());
      this.checkAndSetTrainingMaxScore(lastLevelEvent.getTotalTrainingScore());
      this.checkAndSetAssessmentMaxScore(lastLevelEvent.getTotalAssessmentScore());
      this.numOfFinishedTrainingRuns++;
    }

    public void checkAndSetMaxTime(long maxTime) {
      if (maxTime > this.maxTime) {
        this.maxTime = maxTime;
      }
    }

    private void checkAndSetMaxScore(int maxScore) {
      if (maxScore > this.maxScore) {
        this.maxScore = maxScore;
      }
    }

    private void checkAndSetTrainingMaxScore(int maxTrainingScore) {
      if (maxTrainingScore > this.maxTrainingScore) {
        this.maxTrainingScore = maxTrainingScore;
      }
    }

    private void checkAndSetAssessmentMaxScore(int maxAssessmentScore) {
      if (maxAssessmentScore > this.maxAssessmentScore) {
        this.maxAssessmentScore = maxAssessmentScore;
      }
    }

    public long getMaxTime() {
      return maxTime;
    }

    public int getMaxScore() {
      return maxScore;
    }

    public int getMaxTrainingScore() {
      return maxTrainingScore;
    }

    public int getMaxAssessmentScore() {
      return maxAssessmentScore;
    }

    public float getAverageTime() {
      return sumOfTrainingRunsTime == 0 ? 0 : sumOfTrainingRunsTime / numOfFinishedTrainingRuns;
    }

    public float getAverageScore() {
      return (sumOfTrainingRunsTrainingScore + sumOfTrainingRunsAssessmentScore) == 0
          ? 0
          : (sumOfTrainingRunsTrainingScore + sumOfTrainingRunsAssessmentScore)
              / numOfFinishedTrainingRuns;
    }

    public float getAverageTrainingScore() {
      return sumOfTrainingRunsTrainingScore == 0
          ? 0
          : sumOfTrainingRunsTrainingScore / numOfFinishedTrainingRuns;
    }

    public float getAverageAssessmentScore() {
      return sumOfTrainingRunsAssessmentScore == 0
          ? 0
          : sumOfTrainingRunsAssessmentScore / numOfFinishedTrainingRuns;
    }
  }

  private class LevelStatistics {
    float sumOfLevelsTime;
    float sumOfLevelsScore;
    long maxTime;
    int maxScore;
    int numOfParticipants;

    public void updateStatistics(long trainingTime, int levelScore) {
      this.sumOfLevelsTime += trainingTime;
      this.sumOfLevelsScore += levelScore;
      this.numOfParticipants++;
      this.checkAndSetMaxTime(trainingTime);
      this.checkAndSetMaxScore(levelScore);
    }

    private void checkAndSetMaxTime(long maxTime) {
      if (maxTime > this.maxTime) {
        this.maxTime = maxTime;
      }
    }

    private void checkAndSetMaxScore(int maxScore) {
      if (maxScore > this.maxScore) {
        this.maxScore = maxScore;
      }
    }

    public long getMaxTime() {
      return maxTime;
    }

    public int getMaxScore() {
      return maxScore;
    }

    public float getAverageTime() {
      return sumOfLevelsTime == 0 ? 0 : sumOfLevelsTime / numOfParticipants;
    }

    public float getAverageScore() {
      return sumOfLevelsScore == 0 ? 0 : sumOfLevelsScore / numOfParticipants;
    }
  }

  private class ProcessedLevelsData {
    private final List<VisualizationAbstractLevelDTO> timelineLevels = new ArrayList<>();
    private AbstractAuditPOJO lastLevelEvent;
  }

  private class ProcessedEventsData {
    private final List<EventDTO> timelineEvents = new ArrayList<>();
    private long solutionDisplayedTime;
    private long correctAnswerTime;
    private AbstractAuditPOJO lastLevelEvent;
  }

  private class TrainingInstanceData {
    TrainingDefinition trainingDefinition;
    TrainingInstance trainingInstance;
    List<AbstractLevel> levels;
    Map<Long, Map<Long, List<AbstractAuditPOJO>>> events;
    Map<Long, UserRefDTO> participantsByTrainingRuns;
  }

  private class TrainingData {
    Long estimatedDuration;
    Map<Long, Map<Long, List<AbstractAuditPOJO>>> events;
    Map<Long, UserRefDTO> participantsByTrainingRuns;

    List<ClusteringLevelDTO> levelsField;

    public TrainingData() {}

    public TrainingData(
        Long estimatedDuration,
        Map<Long, Map<Long, List<AbstractAuditPOJO>>> events,
        Map<Long, UserRefDTO> participantsByTrainingRuns) {
      this.estimatedDuration = estimatedDuration;
      this.events = events;
      this.participantsByTrainingRuns = participantsByTrainingRuns;
    }
  }
}
