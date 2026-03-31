package cz.cyberrange.platform.training.service.services;

import cz.cyberrange.platform.training.api.exceptions.EntityErrorDetail;
import cz.cyberrange.platform.training.api.exceptions.EntityNotFoundException;
import cz.cyberrange.platform.training.persistence.model.AbstractLevel;
import cz.cyberrange.platform.training.persistence.model.AccessLevel;
import cz.cyberrange.platform.training.persistence.model.AssessmentLevel;
import cz.cyberrange.platform.training.persistence.model.InfoLevel;
import cz.cyberrange.platform.training.persistence.model.MitreTechnique;
import cz.cyberrange.platform.training.persistence.model.TrainingDefinition;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.persistence.model.TrainingLevel;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import cz.cyberrange.platform.training.persistence.model.question.QuestionAnswer;
import cz.cyberrange.platform.training.persistence.repository.*;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** The type Export import service. */
@Service
public class ExportImportService {

  private final TrainingDefinitionRepository trainingDefinitionRepository;
  private final AbstractLevelRepository abstractLevelRepository;
  private final AssessmentLevelRepository assessmentLevelRepository;
  private final QuestionAnswerRepository questionAnswerRepository;
  private final InfoLevelRepository infoLevelRepository;
  private final TrainingLevelRepository trainingLevelRepository;
  private final MitreTechniqueRepository mitreTechniqueRepository;
  private final AccessLevelRepository accessLevelRepository;
  private final TrainingInstanceRepository trainingInstanceRepository;
  private final TrainingRunRepository trainingRunRepository;

  /**
   * Instantiates a new Export import service.
   *
   * @param trainingDefinitionRepository the training definition repository
   * @param abstractLevelRepository the abstract level repository
   * @param assessmentLevelRepository the assessment level repository
   * @param infoLevelRepository the info level repository
   * @param trainingLevelRepository the training level repository
   * @param trainingInstanceRepository the training instance repository
   * @param trainingRunRepository the training run repository
   */
  @Autowired
  public ExportImportService(
      TrainingDefinitionRepository trainingDefinitionRepository,
      AbstractLevelRepository abstractLevelRepository,
      AssessmentLevelRepository assessmentLevelRepository,
      QuestionAnswerRepository questionAnswerRepository,
      InfoLevelRepository infoLevelRepository,
      TrainingLevelRepository trainingLevelRepository,
      MitreTechniqueRepository mitreTechniqueRepository,
      AccessLevelRepository accessLevelRepository,
      TrainingInstanceRepository trainingInstanceRepository,
      TrainingRunRepository trainingRunRepository) {
    this.trainingDefinitionRepository = trainingDefinitionRepository;
    this.abstractLevelRepository = abstractLevelRepository;
    this.assessmentLevelRepository = assessmentLevelRepository;
    this.questionAnswerRepository = questionAnswerRepository;
    this.trainingLevelRepository = trainingLevelRepository;
    this.accessLevelRepository = accessLevelRepository;
    this.mitreTechniqueRepository = mitreTechniqueRepository;
    this.infoLevelRepository = infoLevelRepository;
    this.trainingInstanceRepository = trainingInstanceRepository;
    this.trainingRunRepository = trainingRunRepository;
  }

  /**
   * Finds training definition with given id.
   *
   * @param trainingDefinitionId the id of definition to be found.
   * @return the {@link TrainingDefinition} with the given id.
   * @throws EntityNotFoundException if training definition was not found.
   */
  public TrainingDefinition findById(Long trainingDefinitionId) {
    return trainingDefinitionRepository
        .findById(trainingDefinitionId)
        .orElseThrow(
            () ->
                new EntityNotFoundException(
                    new EntityErrorDetail(
                        TrainingDefinition.class,
                        "id",
                        trainingDefinitionId.getClass(),
                        trainingDefinitionId)));
  }

  /**
   * Creates a level and connects it with training definition.
   *
   * @param level the {@link AbstractLevel} to be created.
   * @param definition the {@link TrainingDefinition} to associate level with.
   */
  public void createLevel(AbstractLevel level, TrainingDefinition definition) {
    level.setOrder(abstractLevelRepository.getCurrentMaxOrder(definition.getId()) + 1);
    level.setTrainingDefinition(definition);
    if (level instanceof AssessmentLevel) {
      assessmentLevelRepository.save((AssessmentLevel) level);
    } else if (level instanceof InfoLevel) {
      infoLevelRepository.save((InfoLevel) level);
    } else if (level instanceof AccessLevel) {
      accessLevelRepository.save((AccessLevel) level);
    } else {
      setMitreTechniques((TrainingLevel) level);
      trainingLevelRepository.save((TrainingLevel) level);
    }
  }

  private void setMitreTechniques(TrainingLevel importedLevel) {
    Set<String> techniqueKeys =
        importedLevel.getMitreTechniques().stream()
            .map(MitreTechnique::getTechniqueKey)
            .collect(Collectors.toSet());
    Set<MitreTechnique> resultTechniques =
        mitreTechniqueRepository.findAllByTechniqueKeyIn(techniqueKeys);
    resultTechniques.addAll(importedLevel.getMitreTechniques());

    importedLevel.setMitreTechniques(new HashSet<>());
    resultTechniques.forEach(importedLevel::addMitreTechnique);
  }

  /**
   * Finds training instance with given id.
   *
   * @param trainingInstanceId the id of instance to be found.
   * @return the {@link TrainingInstance} with the given id.
   * @throws EntityNotFoundException if training instance was not found.
   */
  public TrainingInstance findInstanceById(Long trainingInstanceId) {
    return trainingInstanceRepository
        .findById(trainingInstanceId)
        .orElseThrow(
            () ->
                new EntityNotFoundException(
                    new EntityErrorDetail(
                        TrainingInstance.class,
                        "id",
                        trainingInstanceId.getClass(),
                        trainingInstanceId)));
  }

  /**
   * Finds training runs associated with training instance with given id.
   *
   * @param trainingInstanceId the id of instance which runs are to be found.
   * @return the set off all {@link TrainingRun}
   */
  public Set<TrainingRun> findRunsByInstanceId(Long trainingInstanceId) {
    return trainingRunRepository.findAllByTrainingInstanceId(trainingInstanceId);
  }

  /**
   * Gets all answers given by participant to assessment questions.
   *
   * @param trainingRunId the pool id
   * @return the sandbox definition id
   */
  public Map<Long, List<QuestionAnswer>> findQuestionsAnswersOfAssessment(Long trainingRunId) {
    return questionAnswerRepository.getAllByTrainingRunId(trainingRunId).stream()
        .collect(
            Collectors.groupingBy(
                questionAnswer -> questionAnswer.getQuestion().getAssessmentLevel().getId()));
  }
}
