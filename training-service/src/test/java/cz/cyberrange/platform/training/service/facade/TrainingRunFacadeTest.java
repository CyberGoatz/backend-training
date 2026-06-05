package cz.cyberrange.platform.training.service.facade;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import cz.cyberrange.platform.training.api.dto.CorrectAnswerDTO;
import cz.cyberrange.platform.training.api.dto.IsCorrectAnswerDTO;
import cz.cyberrange.platform.training.api.dto.UserRefDTO;
import cz.cyberrange.platform.training.api.exceptions.EntityConflictException;
import cz.cyberrange.platform.training.api.responses.SandboxAnswersInfo;
import cz.cyberrange.platform.training.api.responses.VariantAnswer;
import cz.cyberrange.platform.training.persistence.model.AbstractLevel;
import cz.cyberrange.platform.training.persistence.model.AssessmentLevel;
import cz.cyberrange.platform.training.persistence.model.Hint;
import cz.cyberrange.platform.training.persistence.model.InfoLevel;
import cz.cyberrange.platform.training.persistence.model.TrainingDefinition;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.persistence.model.TrainingLevel;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import cz.cyberrange.platform.training.persistence.model.UserRef;
import cz.cyberrange.platform.training.persistence.util.TestDataFactory;
import cz.cyberrange.platform.training.service.mapping.mapstruct.*;
import cz.cyberrange.platform.training.service.services.SecurityService;
import cz.cyberrange.platform.training.service.services.TrainingDefinitionService;
import cz.cyberrange.platform.training.service.services.TrainingRunService;
import cz.cyberrange.platform.training.service.services.UserService;
import cz.cyberrange.platform.training.service.services.api.AnswersStorageApiService;
import cz.cyberrange.platform.training.service.services.api.SandboxApiService;
import cz.cyberrange.platform.training.service.services.api.TrainingFeedbackApiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@SpringBootTest(classes = {
        TestDataFactory.class,
        TrainingDefinitionMapperImpl.class,
        UserRefMapperImpl.class,
        LevelMapperImpl.class,
        HintMapperImpl.class,
        TrainingRunMapperImpl.class,
        BetaTestingGroupMapperImpl.class,
        QuestionMapperImpl.class,
        AttachmentMapperImpl.class,
        ReferenceSolutionNodeMapperImpl.class,
        MitreTechniqueMapperImpl.class
})
public class TrainingRunFacadeTest {

    @Autowired
    TrainingRunMapperImpl trainingRunMapper;
    @Autowired
    LevelMapperImpl levelMapper;
    @Autowired
    HintMapperImpl hintMapper;
    @Autowired
    TestDataFactory testDataFactory;

    private TrainingRunFacade trainingRunFacade;

    @MockBean
    private TrainingRunService trainingRunService;
    @MockBean
    private TrainingDefinitionService trainingDefinitionService;
    @MockBean
    private SecurityService securityService;
    @MockBean
    private UserService userService;
    @MockBean
    private AnswersStorageApiService answersStorageApiService;
    @MockBean
    private SandboxApiService sandboxApiService;
    @MockBean
    private TrainingFeedbackApiService trainingFeedbackApiService;

    private TrainingRun trainingRun1, trainingRun2;
    private Hint hint;
    private TrainingLevel trainingLevel;
    private InfoLevel infoLevel;
    private AssessmentLevel assessmentLevel;
    private UserRefDTO participantRefDTO;
    private UserRef participant;
    private TrainingInstance trainingInstance;
    private TrainingDefinition trainingDefinition;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        trainingRunFacade = new TrainingRunFacade(trainingRunService, trainingDefinitionService, answersStorageApiService, sandboxApiService,
                securityService, userService, trainingFeedbackApiService, trainingRunMapper, levelMapper, hintMapper);

        participant = new UserRef();
        participant.setUserRefId(5L);
        participant.setId(1L);

        trainingRun1 = testDataFactory.getRunningRun();
        trainingRun1.setId(1L);
        trainingRun1.setParticipantRef(participant);

        hint = testDataFactory.getHint1();
        hint.setId(1L);

        participantRefDTO = new UserRefDTO();
        participantRefDTO.setUserRefSub("mail@test.cz");
        participantRefDTO.setUserRefId(2L);

        trainingRun2 = testDataFactory.getFinishedRun();
        trainingRun2.setId(2L);

        trainingDefinition = testDataFactory.getReleasedDefinition();
        trainingDefinition.setId(1L);

        trainingLevel = testDataFactory.getPenalizedLevel();
        trainingLevel.setId(1L);
        trainingLevel.setTrainingDefinition(trainingDefinition);
        trainingLevel.setOrder(0);
        trainingRun1.setCurrentLevel(trainingLevel);

        assessmentLevel = testDataFactory.getTest();
        assessmentLevel.setId(2L);
        assessmentLevel.setOrder(1);
        assessmentLevel.setTrainingDefinition(trainingDefinition);

        infoLevel = testDataFactory.getInfoLevel1();
        infoLevel.setId(3L);
        infoLevel.setOrder(2);
        infoLevel.setTrainingDefinition(trainingDefinition);

        trainingInstance = testDataFactory.getConcludedInstance();
        trainingInstance.setId(1L);
        trainingInstance.setTrainingDefinition(trainingDefinition);
        trainingRun1.setTrainingInstance(trainingInstance);
    }

    @Test
    public void findTrainingRunById() {
        given(trainingRunService.findById(any(Long.class))).willReturn(trainingRun1);
        given(userService.getUserRefDTOByUserRefId(anyLong())).willReturn(participantRefDTO);
        trainingRunFacade.findById(trainingRun1.getId());
        then(trainingRunService).should().findById(trainingRun1.getId());
    }

    @Test
    public void findAllTrainingRuns() {
        List<TrainingRun> expected = new ArrayList<>();
        expected.add(trainingRun1);
        expected.add(trainingRun2);

        Page<TrainingRun> p = new PageImpl<TrainingRun>(expected);

        PathBuilder<TrainingRun> tR = new PathBuilder<TrainingRun>(TrainingRun.class, "trainingRun");
        Predicate predicate = tR.isNotNull();

        given(trainingRunService.findAll(any(Predicate.class), any(Pageable.class))).willReturn(p);
    }

    @Test
    public void isCorrectAnswerBeforeSolutionTaken() {
        given(trainingRunService.isCorrectAnswer(trainingRun1.getId(), "answer")).willReturn(true);
        given(trainingRunService.getRemainingAttempts(trainingRun1.getId())).willReturn(1);
        IsCorrectAnswerDTO correctAnswerDTO = trainingRunFacade.isCorrectAnswer(trainingRun1.getId(), "answer");
        assertTrue(correctAnswerDTO.isCorrect());
        assertEquals(1, correctAnswerDTO.getRemainingAttempts());
    }

    @Test
    public void isCorrectAnswerAfterSolutionTaken() {
        given(trainingRunService.isCorrectAnswer(trainingRun1.getId(), "answer")).willReturn(false);
        IsCorrectAnswerDTO correctAnswerDTO = trainingRunFacade.isCorrectAnswer(trainingRun1.getId(), "answer");
        assertFalse(correctAnswerDTO.isCorrect());
        assertEquals(0, correctAnswerDTO.getRemainingAttempts());
    }

    @Test
    public void accessTrainingRun() {
        given(trainingRunService.getTrainingInstanceForParticularAccessToken(anyString())).willReturn(trainingInstance);
        given(securityService.getUserRefIdFromUserAndGroup()).willReturn(1L);
        given(trainingRunService.findRunningTrainingRunOfUser(anyString(), anyLong())).willReturn(Optional.empty());
        given(trainingRunService.createTrainingRun(trainingInstance, 1L)).willReturn(trainingRun1);
        trainingRunFacade.accessTrainingRun("password");
        then(trainingRunService).should().trAcquisitionLockToPreventManyRequestsFromSameUser(1l, trainingInstance.getId(), "password");
        then(trainingRunService).should().createTrainingRun(trainingInstance, 1L);
    }

    @Test
    public void accessRunningTrainingRun() {
        given(trainingRunService.getTrainingInstanceForParticularAccessToken(anyString())).willReturn(trainingInstance);
        given(securityService.getUserRefIdFromUserAndGroup()).willReturn(1L);
        given(trainingRunService.findRunningTrainingRunOfUser(anyString(), anyLong())).willReturn(Optional.of(trainingRun1));
        given(trainingRunService.resumeTrainingRun(anyLong())).willReturn(trainingRun1);
        trainingRunFacade.accessTrainingRun("password");
        then(trainingRunService).should().resumeTrainingRun(anyLong());
    }

    @Test
    public void accessFinishedTrainingRunThrowsConflict() {
        given(trainingRunService.getTrainingInstanceForParticularAccessToken(anyString())).willReturn(trainingInstance);
        given(securityService.getUserRefIdFromUserAndGroup()).willReturn(1L);
        given(trainingRunService.findRunningTrainingRunOfUser(anyString(), anyLong())).willReturn(Optional.empty());
        given(trainingRunService.hasFinishedTrainingRunOfUser("password", 1L)).willReturn(true);

        assertThrows(EntityConflictException.class, () -> trainingRunFacade.accessTrainingRun("password"));

        then(trainingRunService).should(never()).trAcquisitionLockToPreventManyRequestsFromSameUser(anyLong(), anyLong(), anyString());
        then(trainingRunService).should(never()).createTrainingRun(any(TrainingInstance.class), anyLong());
    }

    @Test
    public void deleteTrainingRun(){
        given(trainingRunService.deleteTrainingRun(trainingRun1.getId(), true, true)).willReturn(trainingRun1);
        trainingRunFacade.deleteTrainingRun(trainingRun1.getId(), true);
        then(trainingRunService).should().deleteTrainingRun(trainingRun1.getId(), true, true);
    }

    @Test
    public void getNextLevel() {
        given(trainingRunService.moveToNextLevel(trainingRun1.getId())).willReturn(trainingRun1);
        trainingRunFacade.getNextLevel(trainingRun1.getId());
        then(trainingRunService).should().moveToNextLevel(trainingRun1.getId());
    }

    @Test
    public void getHint() {
        given(trainingRunService.getHint(anyLong(), anyLong())).willReturn(hint);
        trainingRunFacade.getHint(1L, 1L);
        then(trainingRunService).should().getHint(1L, 1L);
    }

    @Test
    public void getSolution() {
        given(trainingRunService.getSolution(1L)).willReturn("game solution");
        trainingRunFacade.getSolution(trainingRun1.getId());
        then(trainingRunService).should().getSolution(1L);
    }

    @Test
    public void finishTrainingRun() {
        trainingRun1.setSandboxInstanceAllocationId(null);
        given(trainingRunService.finishTrainingRun(trainingRun1.getId())).willReturn(trainingRun1);
        given(trainingDefinitionService.getAllTrainingLevels(trainingDefinition.getId())).willReturn(new ArrayList<>());
        trainingRunFacade.finishTrainingRun(trainingRun1.getId());
        then(trainingRunService).should().finishTrainingRun(trainingRun1.getId());
        then(sandboxApiService).should(never()).cleanupSandboxAllocationUnit(anyInt(), anyBoolean(), anyBoolean());
        then(trainingFeedbackApiService).should(never()).createTraineeGraph(any(), any(), any(), any(), any());
        then(trainingFeedbackApiService).should(never()).createSummaryGraph(any(), any());
        then(trainingFeedbackApiService).should(never()).deleteSummaryGraph(any());
    }

    @Test
    public void finishTrainingRunCleanupAndReplaceCloudSandbox() {
        trainingInstance.setLocalEnvironment(false);
        trainingRun1.setSandboxInstanceAllocationId(10);
        given(trainingRunService.finishTrainingRun(trainingRun1.getId())).willReturn(trainingRun1);
        given(trainingDefinitionService.getAllTrainingLevels(trainingDefinition.getId())).willReturn(new ArrayList<>());

        trainingRunFacade.finishTrainingRun(trainingRun1.getId());

        then(sandboxApiService).should().cleanupSandboxAllocationUnit(10, true, true);
    }

    @Test
    public void getParticipant() {
        given(trainingRunService.findById(trainingRun1.getId())).willReturn(trainingRun1);
        given(userService.getUserRefDTOByUserRefId(participant.getUserRefId())).willReturn(participantRefDTO);
        UserRefDTO foundParticipantRefDTO = trainingRunFacade.getParticipant(trainingRun1.getId());
        assertEquals(participantRefDTO, foundParticipantRefDTO);
    }

    @Test
    public void getCorrectAnswers() {
        TrainingLevel trainingLevelVariantAnswer = testDataFactory.getNonPenalizedLevel();
        trainingLevelVariantAnswer.setId(6L);
        trainingLevelVariantAnswer.setVariantAnswers(true);
        trainingLevelVariantAnswer.setAnswerVariableName("username");
        trainingLevelVariantAnswer.setAnswer(null);
        VariantAnswer variantAnswer = new VariantAnswer();
        variantAnswer.setAnswerVariableName("username");
        variantAnswer.setAnswerContent("john");
        SandboxAnswersInfo sandboxAnswersInfo = new SandboxAnswersInfo();
        sandboxAnswersInfo.setSandboxRefId(trainingRun1.getSandboxInstanceRefId());
        sandboxAnswersInfo.setVariantAnswers(new ArrayList<>(List.of(variantAnswer)));

        List<AbstractLevel> levels = new ArrayList<>(List.of(trainingLevel, infoLevel, assessmentLevel, trainingLevelVariantAnswer));
        given(trainingRunService.findByIdWithLevel(trainingRun1.getId())).willReturn(trainingRun1);
        given(trainingRunService.getLevels(trainingDefinition.getId())).willReturn(levels);
        given(answersStorageApiService.getAnswersBySandboxId(trainingRun1.getSandboxInstanceRefId())).willReturn(sandboxAnswersInfo);
        List<CorrectAnswerDTO> correctAnswers = trainingRunFacade.getCorrectAnswers(trainingRun1.getId());

        assertEquals(2, correctAnswers.size());
        assertTrue(correctAnswers.containsAll(List.of(
                getCorrectAnswerDTO(trainingLevel, null),
                getCorrectAnswerDTO(trainingLevelVariantAnswer, variantAnswer.getAnswerContent()))
                )
        );
    }

    private CorrectAnswerDTO getCorrectAnswerDTO(TrainingLevel trainingLevel, String correctVariantAnswer) {
        CorrectAnswerDTO answerDTO = new CorrectAnswerDTO();
        answerDTO.setLevelId(trainingLevel.getId());
        answerDTO.setLevelTitle(trainingLevel.getTitle());
        answerDTO.setLevelOrder(trainingLevel.getOrder());
        answerDTO.setCorrectAnswer(correctVariantAnswer == null ? trainingLevel.getAnswer() : correctVariantAnswer);
        answerDTO.setVariableName(trainingLevel.getAnswerVariableName());
        return answerDTO;
    }
}
