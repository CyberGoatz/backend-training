package cz.cyberrange.platform.training.service.services;

import cz.cyberrange.platform.training.api.exceptions.EntityConflictException;
import cz.cyberrange.platform.training.api.exceptions.EntityErrorDetail;
import cz.cyberrange.platform.training.persistence.model.AbstractLevel;
import cz.cyberrange.platform.training.persistence.model.AssessmentLevel;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.persistence.model.TrainingLevel;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import cz.cyberrange.platform.training.persistence.model.enums.TRState;
import cz.cyberrange.platform.training.persistence.model.question.QuestionAnswer;
import cz.cyberrange.platform.training.persistence.repository.AbstractLevelRepository;
import cz.cyberrange.platform.training.persistence.repository.AssessmentLevelRepository;
import cz.cyberrange.platform.training.persistence.repository.QuestionAnswerRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingLevelRepository;
import cz.cyberrange.platform.training.persistence.repository.UserRefRepository;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** The type Visualization service. */
@Service
public class VisualizationService {

  private final AbstractLevelRepository abstractLevelRepository;
  private final UserRefRepository userRefRepository;
  private final TrainingLevelRepository trainingLevelRepository;
  private final AssessmentLevelRepository assessmentLevelRepository;
  private final QuestionAnswerRepository questionAnswerRepository;

  /**
   * Instantiates a new Visualization service.
   *
   * @param abstractLevelRepository the abstract level repository
   * @param userRefRepository the user ref repository
   */
  @Autowired
  public VisualizationService(
      AbstractLevelRepository abstractLevelRepository,
      UserRefRepository userRefRepository,
      TrainingLevelRepository trainingLevelRepository,
      AssessmentLevelRepository assessmentLevelRepository,
      QuestionAnswerRepository questionAnswerRepository) {
    this.abstractLevelRepository = abstractLevelRepository;
    this.userRefRepository = userRefRepository;
    this.trainingLevelRepository = trainingLevelRepository;
    this.assessmentLevelRepository = assessmentLevelRepository;
    this.questionAnswerRepository = questionAnswerRepository;
  }

  /**
   * Gets list of all levels for trainee of given Training Run.
   *
   * @param trainingRun the training run for which to find all levels.
   * @return List of {@link AbstractLevel}s
   * @throws EntityConflictException training run is still running
   */
  public List<AbstractLevel> getLevelsForTraineeVisualization(TrainingRun trainingRun) {
    if (trainingRun.getState().equals(TRState.RUNNING)) {
      throw new EntityConflictException(
          new EntityErrorDetail(
              TrainingRun.class,
              "id",
              trainingRun.getId().getClass(),
              trainingRun.getId(),
              "Logged in user cannot access info for visualization because training run is still running."));
    }
    return abstractLevelRepository.findAllLevelsByTrainingDefinitionId(
        trainingRun.getTrainingInstance().getTrainingDefinition().getId());
  }

  /**
   * Gets list of all levels for organizer of given Training Instance.
   *
   * @param trainingInstance the training instance for which to find all levels.
   * @return List of {@link AbstractLevel}s
   */
  public List<AbstractLevel> getLevelsForOrganizerVisualization(TrainingInstance trainingInstance) {
    return abstractLevelRepository.findAllLevelsByTrainingDefinitionId(
        trainingInstance.getTrainingDefinition().getId());
  }

  /**
   * Get all participants ref ids in given training instance.
   *
   * @param trainingInstanceId id of Training Instance to gets participants ref ids.
   * @return list of participants ref ids.
   */
  public Set<Long> getAllParticipantsRefIdsForSpecificTrainingInstance(Long trainingInstanceId) {
    return userRefRepository.findParticipantsRefIdsByTrainingInstanceId(trainingInstanceId);
  }

  public List<TrainingLevel> getAllTrainingLevels() {
    return trainingLevelRepository.findAll();
  }

  public List<TrainingLevel> getTrainingLevelsByTrainingDefinitionId(Long trainingDefinitionId) {
    return trainingLevelRepository.findAllByTrainingDefinitionId(trainingDefinitionId);
  }

  public List<AssessmentLevel> getAssessmentLevelsByTrainingDefinitionId(
      Long trainingDefinitionId) {
    return assessmentLevelRepository.findAllByTrainingDefinitionId(trainingDefinitionId);
  }

  public List<QuestionAnswer> getAnswersToQuestionByTrainingInstance(
      Long questionId, Long instanceId) {
    return questionAnswerRepository.getAllByQuestionIdAndInstanceId(questionId, instanceId);
  }
}
