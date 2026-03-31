package cz.cyberrange.platform.training.persistence.repository;

import static org.junit.jupiter.api.Assertions.*;

import cz.cyberrange.platform.training.persistence.model.AbstractLevel;
import cz.cyberrange.platform.training.persistence.model.AssessmentLevel;
import cz.cyberrange.platform.training.persistence.model.Hint;
import cz.cyberrange.platform.training.persistence.model.InfoLevel;
import cz.cyberrange.platform.training.persistence.model.TrainingDefinition;
import cz.cyberrange.platform.training.persistence.model.TrainingLevel;
import cz.cyberrange.platform.training.persistence.util.TestDataFactory;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
public class AbstractLevelRepositoryTest {

  @Autowired private TestEntityManager entityManager;
  @Autowired private AbstractLevelRepository abstractLevelRepository;
  @Autowired private HintRepository hintRepository;
  @Autowired private TestDataFactory testDataFactory;

  private TrainingLevel trainingLevel, trainingLevel2;
  private AssessmentLevel assessmentLevel;
  private InfoLevel infoLevel, infoLevel2;
  private TrainingDefinition trainingDefinition;
  private Hint hint;

  @BeforeEach
  public void setUp() {
    trainingDefinition = testDataFactory.getUnreleasedDefinition();
    entityManager.persistAndFlush(trainingDefinition);

    trainingLevel = testDataFactory.getPenalizedLevel();
    trainingLevel.setTrainingDefinition(trainingDefinition);
    trainingLevel.setOrder(0);
    trainingLevel2 = testDataFactory.getNonPenalizedLevel();
    trainingLevel2.setTrainingDefinition(trainingDefinition);
    trainingLevel2.setOrder(1);

    hint = new Hint();
    hint.setContent("sadas");
    hint.setOrder(1);
    hint.setTitle("tttt");
    hint.setHintPenalty(5);
    hint.setTrainingLevel(trainingLevel);

    assessmentLevel = testDataFactory.getTest();
    assessmentLevel.setTrainingDefinition(trainingDefinition);
    assessmentLevel.setOrder(2);

    infoLevel = testDataFactory.getInfoLevel1();
    infoLevel.setTrainingDefinition(trainingDefinition);
    infoLevel.setOrder(3);

    infoLevel2 = testDataFactory.getInfoLevel2();
    infoLevel2.setTrainingDefinition(trainingDefinition);
    infoLevel2.setOrder(4);
  }

  @Test
  public void findByIdTrainingLevel() {
    Long id = (Long) entityManager.persistAndGetId(trainingLevel);
    entityManager.persistAndFlush(hint);
    entityManager.persist(assessmentLevel);
    entityManager.persist(infoLevel);
    Optional<AbstractLevel> optionalTrainingLevel = abstractLevelRepository.findById(id);
    assertTrue(optionalTrainingLevel.isPresent());
    assertTrue(optionalTrainingLevel.get() instanceof TrainingLevel);
    assertEquals(trainingLevel, optionalTrainingLevel.get());
  }

  @Test
  public void findByIdInfoLevel() {
    Long id = (Long) entityManager.persistAndGetId(infoLevel);
    entityManager.persist(trainingLevel);
    entityManager.persist(assessmentLevel);
    Optional<AbstractLevel> optionalInfoLevel = abstractLevelRepository.findById(id);
    assertTrue(optionalInfoLevel.isPresent());
    assertTrue(optionalInfoLevel.get() instanceof InfoLevel);
    assertEquals(infoLevel, optionalInfoLevel.get());
  }

  @Test
  public void findByIdAssessmentLevel() {
    Long id = (Long) entityManager.persistAndGetId(assessmentLevel);
    entityManager.persist(trainingLevel2);
    entityManager.persist(trainingLevel);
    entityManager.persist(infoLevel);
    Optional<AbstractLevel> optionalAssesmentLevel = abstractLevelRepository.findById(id);
    assertTrue(optionalAssesmentLevel.isPresent());
    assertTrue(optionalAssesmentLevel.get() instanceof AssessmentLevel);
    assertEquals(assessmentLevel, optionalAssesmentLevel.get());
  }

  @Test
  public void findByIdTrainingLevel_multipleOccurrences() {
    entityManager.persist(assessmentLevel);
    entityManager.persist(trainingLevel);
    entityManager.persist(infoLevel);
    Long id = (Long) entityManager.persistAndGetId(trainingLevel2);
    entityManager.persist(infoLevel2);
    Optional<AbstractLevel> optionalTrainingLevel = abstractLevelRepository.findById(id);
    assertTrue(optionalTrainingLevel.isPresent());
    assertTrue(optionalTrainingLevel.get() instanceof TrainingLevel);
    assertEquals(trainingLevel2, optionalTrainingLevel.get());
  }

  @Test
  public void findAll() {
    entityManager.persist(trainingDefinition);
    List<AbstractLevel> expectedAbstractLevels =
        Arrays.asList(trainingLevel, infoLevel, assessmentLevel, infoLevel2, trainingLevel2);
    expectedAbstractLevels.stream().forEach(a -> entityManager.persist(a));
    List<AbstractLevel> resultAbstractLevels = abstractLevelRepository.findAll();
    assertNotNull(resultAbstractLevels);
    assertEquals(expectedAbstractLevels.size(), resultAbstractLevels.size());
    assertEquals(expectedAbstractLevels, resultAbstractLevels);
  }

  @Test
  public void getCurrentMaxOrder() {
    entityManager.persist(trainingDefinition);
    entityManager.persist(trainingLevel);
    entityManager.persist(trainingLevel2);
    entityManager.persist(assessmentLevel);
    entityManager.persist(infoLevel);
    entityManager.persist(infoLevel2);

    int maxOrder = abstractLevelRepository.getCurrentMaxOrder(trainingDefinition.getId());
    assertEquals(maxOrder, 4);
  }

  @Test
  public void getCurrentMaxOrderWithNoLevels() {
    entityManager.persist(trainingDefinition);
    int maxOrder = abstractLevelRepository.getCurrentMaxOrder(trainingDefinition.getId());
    assertEquals(maxOrder, -1);
  }
}
