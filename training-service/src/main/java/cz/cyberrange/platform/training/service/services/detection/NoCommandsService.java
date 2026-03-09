package cz.cyberrange.platform.training.service.services.detection;

import cz.cyberrange.platform.training.opensearch.model.AbstractAuditPOJO;
import cz.cyberrange.platform.training.persistence.model.Submission;
import cz.cyberrange.platform.training.persistence.model.TrainingLevel;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import cz.cyberrange.platform.training.persistence.model.detection.CheatingDetection;
import cz.cyberrange.platform.training.persistence.model.detection.DetectionEventParticipant;
import cz.cyberrange.platform.training.persistence.model.detection.NoCommandsDetectionEvent;
import cz.cyberrange.platform.training.persistence.model.enums.DetectionEventType;
import cz.cyberrange.platform.training.persistence.repository.SubmissionRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingLevelRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingRunRepository;
import cz.cyberrange.platform.training.persistence.repository.detection.NoCommandsDetectionEventRepository;
import cz.cyberrange.platform.training.service.services.TrainingInstanceService;
import cz.cyberrange.platform.training.service.services.TrainingRunService;
import cz.cyberrange.platform.training.service.services.api.OpenSearchApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static cz.cyberrange.platform.training.service.utils.CheatingDetectionUtils.extractParticipant;
import static cz.cyberrange.platform.training.service.utils.CheatingDetectionUtils.generateParticipantString;

@Service
public class NoCommandsService {
    private static final Logger LOG = LoggerFactory.getLogger(CheatingDetectionService.class);
    private final TrainingLevelRepository trainingLevelRepository;
    private final TrainingRunRepository trainingRunRepository;
    private final SubmissionRepository submissionRepository;
    private final NoCommandsDetectionEventRepository noCommandsDetectionEventRepository;
    private final TrainingRunService trainingRunService;
    private final TrainingInstanceService trainingInstanceService;
    private final OpenSearchApiService opensearchApiService;
    private final DetectionEventService detectionEventService;

    /**
     * Instantiates a new Cheating detection service.
     *
     * @param trainingLevelRepository            the training level repository
     * @param trainingRunRepository              the training run repository
     * @param submissionRepository               the submission repository
     * @param noCommandsDetectionEventRepository the no commands detection event repository
     * @param trainingRunService                 the training run service
     * @param trainingInstanceService            the training instance service
     * @param opensearchApiService            the elastic search api service
     */
    @Autowired
    public NoCommandsService(TrainingLevelRepository trainingLevelRepository,
                             TrainingRunRepository trainingRunRepository,
                             SubmissionRepository submissionRepository,
                             NoCommandsDetectionEventRepository noCommandsDetectionEventRepository,
                             TrainingRunService trainingRunService,
                             OpenSearchApiService opensearchApiService,
                             TrainingInstanceService trainingInstanceService,
                             DetectionEventService detectionEventService) {
        this.trainingLevelRepository = trainingLevelRepository;
        this.trainingRunRepository = trainingRunRepository;
        this.submissionRepository = submissionRepository;
        this.noCommandsDetectionEventRepository = noCommandsDetectionEventRepository;
        this.trainingRunService = trainingRunService;
        this.opensearchApiService = opensearchApiService;
        this.trainingInstanceService = trainingInstanceService;
        this.detectionEventService = detectionEventService;
    }

    /**
     * finds all no commands event of cheating detection
     *
     * @param cheatingDetectionId the cheating detection id
     * @return events
     */
    public List<NoCommandsDetectionEvent> findAllNoCommandsEventsOfDetection(Long cheatingDetectionId) {
        return noCommandsDetectionEventRepository.findAllByCheatingDetectionId(cheatingDetectionId);
    }

    /**
     * finds no command event by id
     *
     * @param eventId the event id
     * @return event
     */
    public NoCommandsDetectionEvent findNoCommandsEventById(Long eventId) {
        return noCommandsDetectionEventRepository.findNoCommandsEventById(eventId);
    }

    /**
     * Executes a cheating detection of type NO_COMMANDS
     *
     * @param cd the training instance id
     */
    void executeCheatingDetectionOfNoCommands(CheatingDetection cd) {
        Long trainingInstanceId = cd.getTrainingInstanceId();
        Map<Long, TrainingLevel> trainingLevelsById = trainingLevelRepository
                .findAllByTrainingDefinitionId(trainingInstanceService.findById(trainingInstanceId).getTrainingDefinition().getId())
                .stream()
                .collect(Collectors.toMap(TrainingLevel::getId, level -> level));
        Map<Long, List<Submission>> detectedSubmissionsByLevels = new HashMap<>();
        for (var run : new ArrayList<>(trainingRunService.findAllByTrainingInstanceId(trainingInstanceId))) {
            executeNoCommandsDetectionForRun(trainingLevelsById, detectedSubmissionsByLevels, run);
        }
        Set<DetectionEventParticipant> participants;
        for (var submissions : detectedSubmissionsByLevels.entrySet()) {
            participants = new HashSet<>();
            generateParticipantsOfEvent(participants, submissions);
            auditNoCommandsEvent(submissions.getValue().get(0), cd, participants);
        }

    }

    private boolean wasSolutionDisplayed(List<AbstractAuditPOJO> events, Submission submission) {
        for (AbstractAuditPOJO event : events) {
            if (event.getLevel() == submission.getLevel().getId() && event.getType().contains("SolutionDisplayed")) {
                return true;
            }
        }
        return false;
    }

    private void generateParticipantsOfEvent(Set<DetectionEventParticipant> participants, Map.Entry<Long, List<Submission>> submissions) {
        for (var submission : submissions.getValue()) {
            participants.add(extractParticipant(submission, detectionEventService.getUserFullName(submission)));
            trainingRunService.auditRunHasDetectionEvent(submission.getTrainingRun());
        }
    }

    private void executeNoCommandsDetectionForRun(Map<Long, TrainingLevel> trainingLevelsById, Map<Long, List<Submission>> detectedSubmissionsByLevels, TrainingRun run) {
        List<Submission> submissions;
        List<AbstractAuditPOJO> events = opensearchApiService.findAllEventsFromTrainingRun(run);
        submissions = submissionRepository.getCorrectSubmissionsOfTrainingRunSorted(run.getId());
        for (int i = 0; i < submissions.size() - 1; i++) {
            evaluateNoCommandsSubmissionsOfTrainingRun(trainingLevelsById, detectedSubmissionsByLevels, run, submissions, events, i);
        }
    }

    private void evaluateNoCommandsSubmissionsOfTrainingRun(Map<Long, TrainingLevel> trainingLevelsById, Map<Long, List<Submission>> detectedSubmissionsByLevels, TrainingRun run, List<Submission> submissions, List<AbstractAuditPOJO> events, int i) {
        Submission submission;
        LocalDateTime from;
        submission = submissions.get(i);
        from = (i == 0) ? run.getStartTime() : submissions.get(i - 1).getDate();
        Long currentId = submission.getLevel().getId();
        if (!trainingLevelsById.containsKey(currentId) || !trainingLevelsById.get(currentId).isCommandsRequired()) {
            return;
        }
        if (wasSolutionDisplayed(events, submission)) {
            return;
        }
        if (evalCheatOfNoCommands(run.getSandboxInstanceRefId(), from, submission)) {
            if (detectedSubmissionsByLevels.containsKey(submission.getLevel().getId())) {
                var tempSubmissions = detectedSubmissionsByLevels.get(submission.getLevel().getId());
                tempSubmissions.add(submission);
                detectedSubmissionsByLevels.put(submission.getLevel().getId(), tempSubmissions);
            } else {
                List<Submission> subs = new ArrayList<>();
                subs.add(submission);
                detectedSubmissionsByLevels.put(submission.getLevel().getId(), subs);
            }
        }
    }

    private boolean evalCheatOfNoCommands(String sandboxId, LocalDateTime from, Submission submission) {
        long fromMilli = from.atZone(ZoneOffset.UTC).toInstant().toEpochMilli();
        long toMilli = submission.getDate().atZone(ZoneOffset.UTC).toInstant().toEpochMilli();
        return opensearchApiService.findAllConsoleCommandsBySandboxAndTimeRange(sandboxId, fromMilli, toMilli).isEmpty();
    }

    private void auditNoCommandsEvent(Submission submission, CheatingDetection cd, Set<DetectionEventParticipant> participants) {
        TrainingRun run = submission.getTrainingRun();
        run.setHasDetectionEvent(true);
        trainingRunRepository.save(run);
        NoCommandsDetectionEvent event = new NoCommandsDetectionEvent();
        event.setCommonDetectionEventParameters(submission, cd, DetectionEventType.NO_COMMANDS, participants.size());
        event.setParticipants(generateParticipantString(participants));
        detectionEventService.saveParticipants(participants, noCommandsDetectionEventRepository.save(event).getId(), cd.getId());
    }
}
