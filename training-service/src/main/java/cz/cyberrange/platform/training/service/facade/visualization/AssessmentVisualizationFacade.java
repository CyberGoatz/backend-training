package cz.cyberrange.platform.training.service.facade.visualization;

import cz.cyberrange.platform.training.api.dto.UserRefDTO;
import cz.cyberrange.platform.training.api.dto.archive.QuestionEMIAnswer;
import cz.cyberrange.platform.training.api.dto.visualization.assessment.AssessmentVisualizationDTO;
import cz.cyberrange.platform.training.api.dto.visualization.assessment.QuestionVisualizationDTO;
import cz.cyberrange.platform.training.api.dto.visualization.assessment.answer.AbstractAnswerDTO;
import cz.cyberrange.platform.training.api.dto.visualization.assessment.answer.EMIAnswerDTO;
import cz.cyberrange.platform.training.api.dto.visualization.assessment.answer.EMIOptionDTO;
import cz.cyberrange.platform.training.api.dto.visualization.assessment.answer.FFQAnswerDTO;
import cz.cyberrange.platform.training.api.dto.visualization.assessment.answer.MCQAnswerDTO;
import cz.cyberrange.platform.training.api.exceptions.InternalServerErrorException;
import cz.cyberrange.platform.training.api.responses.PageResultResource;
import cz.cyberrange.platform.training.persistence.model.AssessmentLevel;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.persistence.model.enums.AssessmentType;
import cz.cyberrange.platform.training.persistence.model.question.ExtendedMatchingOption;
import cz.cyberrange.platform.training.persistence.model.question.ExtendedMatchingStatement;
import cz.cyberrange.platform.training.persistence.model.question.Question;
import cz.cyberrange.platform.training.persistence.model.question.QuestionAnswer;
import cz.cyberrange.platform.training.persistence.model.question.QuestionChoice;
import cz.cyberrange.platform.training.service.annotations.transactions.TransactionalRO;
import cz.cyberrange.platform.training.service.mapping.mapstruct.LevelMapper;
import cz.cyberrange.platform.training.service.mapping.mapstruct.QuestionMapper;
import cz.cyberrange.platform.training.service.services.TrainingInstanceService;
import cz.cyberrange.platform.training.service.services.UserService;
import cz.cyberrange.platform.training.service.services.VisualizationService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class AssessmentVisualizationFacade {

  private final TrainingInstanceService trainingInstanceService;
  private final VisualizationService visualizationService;
  private final UserService userService;
  private final LevelMapper levelMapper;
  private final QuestionMapper questionMapper;

  public AssessmentVisualizationFacade(
      TrainingInstanceService trainingInstanceService,
      VisualizationService visualizationService,
      UserService userService,
      LevelMapper levelMapper,
      QuestionMapper questionMapper) {
    this.trainingInstanceService = trainingInstanceService;
    this.visualizationService = visualizationService;
    this.userService = userService;
    this.levelMapper = levelMapper;
    this.questionMapper = questionMapper;
  }

  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isOrganizerOfGivenTrainingInstance(#instanceId)")
  @TransactionalRO
  public List<AssessmentVisualizationDTO> getAssessmentVisualizationData(Long instanceId) {
    TrainingInstance trainingInstance = trainingInstanceService.findById(instanceId);
    List<AssessmentLevel> assessmentLevels =
        visualizationService.getAssessmentLevelsByTrainingDefinitionId(
            trainingInstance.getTrainingDefinition().getId());

    List<AssessmentVisualizationDTO> result = new ArrayList<>();
    for (AssessmentLevel assessmentLevel : assessmentLevels) {
      AssessmentVisualizationDTO assessmentVisualizationDTO = new AssessmentVisualizationDTO();
      assessmentVisualizationDTO.setId(assessmentLevel.getId());
      assessmentVisualizationDTO.setTitle(assessmentLevel.getTitle());
      assessmentVisualizationDTO.setOrder(assessmentLevel.getOrder());
      assessmentVisualizationDTO.setAssessmentType(
          levelMapper.mapToApiType(assessmentLevel.getAssessmentType()));

      for (Question question : assessmentLevel.getQuestions()) {
        QuestionVisualizationDTO questionVisualizationDTO = new QuestionVisualizationDTO();
        questionVisualizationDTO.setId(question.getId());
        questionVisualizationDTO.setText(question.getText());
        questionVisualizationDTO.setOrder(question.getOrder());
        questionVisualizationDTO.setQuestionType(
            questionMapper.mapToApiType(question.getQuestionType()));
        questionVisualizationDTO.setAnswers(collectAnswersToQuestion(question, instanceId));
        assessmentVisualizationDTO.addQuestion(questionVisualizationDTO);
      }
      result.add(assessmentVisualizationDTO);
    }
    return result;
  }

  private List<? extends AbstractAnswerDTO> collectAnswersToQuestion(
      Question question, Long instanceId) {
    Map<Long, UserRefDTO> participantsByIds =
        getParticipantsForGivenTrainingInstance(instanceId).stream()
            .collect(Collectors.toMap(UserRefDTO::getUserRefId, Function.identity()));

    return switch (question.getQuestionType()) {
      case FFQ -> collectAnswersForFreeFormQuestion(question, instanceId, participantsByIds);
      case MCQ -> collectAnswersForMultipleChoiceQuestion(question, instanceId, participantsByIds);
      case EMI ->
          collectAnswersForExtendedMatchingItemsQuestion(question, instanceId, participantsByIds);
    };
  }

  private Map<String, List<UserRefDTO>> preProcessQuestionAnswers(
      Long questionId, Long instanceId, Map<Long, UserRefDTO> participantsByIds) {
    Map<String, List<UserRefDTO>> participantsByAnswerText = new HashMap<>();
    visualizationService
        .getAnswersToQuestionByTrainingInstance(questionId, instanceId)
        .forEach(
            questionAnswer ->
                questionAnswer
                    .getAnswers()
                    .forEach(
                        answer -> {
                          List<UserRefDTO> participants =
                              participantsByAnswerText.getOrDefault(answer, new ArrayList<>());
                          participants.add(
                              participantsByIds.get(
                                  questionAnswer
                                      .getTrainingRun()
                                      .getParticipantRef()
                                      .getUserRefId()));
                          participantsByAnswerText.put(answer, participants);
                        }));
    return participantsByAnswerText;
  }

  private Map<String, Map<String, List<UserRefDTO>>> preProcessEMIQuestionAnswers(
      Question question, Long instanceId, Map<Long, UserRefDTO> participantsByIds) {
    Map<String, Map<String, List<UserRefDTO>>> result = new HashMap<>();
    for (QuestionAnswer questionAnswer :
        visualizationService.getAnswersToQuestionByTrainingInstance(question.getId(), instanceId)) {
      for (String answer : questionAnswer.getAnswers()) {
        QuestionEMIAnswer questionEMIAnswer = convertToEMIAnswer(answer);
        String statementText = getStatementText(question, questionEMIAnswer.getStatementOrder());
        String optionText = getOptionText(question, questionEMIAnswer.getOptionOrder());

        Map<String, List<UserRefDTO>> innerResult =
            result.getOrDefault(statementText, new HashMap<>());
        List<UserRefDTO> participants = innerResult.getOrDefault(optionText, new ArrayList<>());

        participants.add(
            participantsByIds.get(
                questionAnswer.getTrainingRun().getParticipantRef().getUserRefId()));
        innerResult.put(optionText, participants);
        result.put(statementText, innerResult);
      }
    }
    return result;
  }

  private String getStatementText(Question question, Integer statementOrderInAnswer) {
    if (statementOrderInAnswer < 0
        || statementOrderInAnswer >= question.getExtendedMatchingStatements().size()) {
      throw new InternalServerErrorException(
          "Statement order (value: "
              + statementOrderInAnswer
              + ") in the user answer is out"
              + " of range of the extended matching statements (size: "
              + question.getExtendedMatchingStatements().size()
              + ") defined in the question.");
    }
    return question.getExtendedMatchingStatements().get(statementOrderInAnswer).getText();
  }

  private String getOptionText(Question question, Integer optionOrderInAnswer) {
    if (optionOrderInAnswer < 0
        || optionOrderInAnswer >= question.getExtendedMatchingOptions().size()) {
      throw new InternalServerErrorException(
          "Option order (value: "
              + optionOrderInAnswer
              + ") in the user answer is out"
              + " of range of the extended matching options (size: "
              + question.getExtendedMatchingOptions().size()
              + ") defined in the question.");
    }
    return question.getExtendedMatchingOptions().get(optionOrderInAnswer).getText();
  }

  private List<FFQAnswerDTO> collectAnswersForFreeFormQuestion(
      Question question, Long instanceId, Map<Long, UserRefDTO> participantsByIds) {
    boolean isTest = question.getAssessmentLevel().getAssessmentType() == AssessmentType.TEST;
    var participantsByAnswerText =
        preProcessQuestionAnswers(question.getId(), instanceId, participantsByIds);
    Set<String> correctAnswers =
        question.getChoices().stream().map(QuestionChoice::getText).collect(Collectors.toSet());
    return participantsByAnswerText.entrySet().stream()
        .map(
            entry ->
                (FFQAnswerDTO)
                    FFQAnswerDTO.builder()
                        .text(entry.getKey())
                        .isCorrect(!isTest || correctAnswers.contains(entry.getKey()))
                        .participants(entry.getValue())
                        .build())
        .toList();
  }

  private List<MCQAnswerDTO> collectAnswersForMultipleChoiceQuestion(
      Question question, Long instanceId, Map<Long, UserRefDTO> participantsByIds) {
    boolean isTest = question.getAssessmentLevel().getAssessmentType() == AssessmentType.TEST;
    var participantsByAnswerText =
        preProcessQuestionAnswers(question.getId(), instanceId, participantsByIds);
    Set<String> correctAnswers =
        question.getChoices().stream()
            .filter(QuestionChoice::isCorrect)
            .map(QuestionChoice::getText)
            .collect(Collectors.toSet());
    return participantsByAnswerText.entrySet().stream()
        .map(
            entry ->
                (MCQAnswerDTO)
                    MCQAnswerDTO.builder()
                        .text(entry.getKey())
                        .isCorrect(!isTest || correctAnswers.contains(entry.getKey()))
                        .participants(entry.getValue())
                        .build())
        .toList();
  }

  private List<EMIAnswerDTO> collectAnswersForExtendedMatchingItemsQuestion(
      Question question, Long instanceId, Map<Long, UserRefDTO> participantsByIds) {
    var participantsByEMIAnswers =
        preProcessEMIQuestionAnswers(question, instanceId, participantsByIds);
    return question.getExtendedMatchingStatements().stream()
        .map(
            statement ->
                (EMIAnswerDTO)
                    EMIAnswerDTO.builder()
                        .text(statement.getText())
                        .options(
                            collectUsersOptionsForStatement(
                                statement,
                                question.getExtendedMatchingOptions(),
                                participantsByEMIAnswers.getOrDefault(
                                    statement.getText(), new HashMap<>())))
                        .build())
        .toList();
  }

  private List<EMIOptionDTO> collectUsersOptionsForStatement(
      ExtendedMatchingStatement statement,
      List<ExtendedMatchingOption> options,
      Map<String, List<UserRefDTO>> participantsByOption) {
    return options.stream()
        .map(
            option ->
                EMIOptionDTO.builder()
                    .text(option.getText())
                    .isCorrect(
                        statement.getExtendedMatchingOption() != null
                            && option.getId().equals(statement.getExtendedMatchingOption().getId()))
                    .participants(
                        participantsByOption.getOrDefault(
                            option.getText(), Collections.emptyList()))
                    .build())
        .toList();
  }

  private List<UserRefDTO> getParticipantsForGivenTrainingInstance(Long instanceId) {
    List<Long> participantsRefIds =
        new ArrayList<>(
            visualizationService.getAllParticipantsRefIdsForSpecificTrainingInstance(instanceId));
    List<UserRefDTO> participants = new ArrayList<>();
    PageResultResource<UserRefDTO> participantsInfo;
    int page = 0;
    do {
      participantsInfo =
          userService.getUsersRefDTOByGivenUserIds(
              participantsRefIds, PageRequest.of(page, 999), null, null);
      participants.addAll(participantsInfo.getContent());
      page++;
    } while (page != participantsInfo.getPagination().getTotalPages());
    return participants;
  }

  private QuestionEMIAnswer convertToEMIAnswer(String emiAnswer) {
    String[] tmp = emiAnswer.replaceAll("\"", "").substring(1, emiAnswer.length() - 5).split(",");
    Integer statementOrder = Integer.parseInt(tmp[0].split(":")[1].trim());
    Integer optionOrder = Integer.parseInt(tmp[1].split(":")[1].trim());
    return new QuestionEMIAnswer(statementOrder, optionOrder);
  }
}
