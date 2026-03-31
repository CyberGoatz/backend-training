package cz.cyberrange.platform.training.persistence.repository;

import static org.junit.jupiter.api.Assertions.*;

import cz.cyberrange.platform.training.persistence.model.TrainingDefinition;
import cz.cyberrange.platform.training.persistence.model.TrainingLevel;
import cz.cyberrange.platform.training.persistence.util.TestDataFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.InvalidDataAccessApiUsageException;

@DataJpaTest
public class TrainingLevelRepositoryTest {

  @Autowired private TestEntityManager entityManager;
  @Autowired private TestDataFactory testDataFactory;
  @Autowired private TrainingLevelRepository trainingLevelRepository;

  private TrainingLevel trainingLevel1, trainingLevel2, trainingLevel3;
  private TrainingDefinition trainingDefinition1, trainingDefinition2;

  @BeforeEach
  public void setUp() {
    trainingDefinition1 = testDataFactory.getArchivedDefinition();
    trainingDefinition2 = testDataFactory.getReleasedDefinition();
    entityManager.persist(trainingDefinition1);
    entityManager.persist(trainingDefinition2);

    trainingLevel1 = testDataFactory.getPenalizedLevel();
    trainingLevel1.setTrainingDefinition(trainingDefinition1);
    trainingLevel2 = testDataFactory.getNonPenalizedLevel();
    trainingLevel2.setTrainingDefinition(trainingDefinition2);
    trainingLevel3 = testDataFactory.getPenalizedLevel();
    trainingLevel3.setTrainingDefinition(trainingDefinition1);
  }

  @Test
  public void findById() {
    Long id = (Long) entityManager.persistAndGetId(trainingLevel1);
    Optional<TrainingLevel> optionalTrainingLevel = trainingLevelRepository.findById(id);
    assertTrue(optionalTrainingLevel.isPresent());
    assertEquals(trainingLevel1, optionalTrainingLevel.get());
  }

  @Test
  public void findByIdNullableArgument() throws Exception {
    entityManager.persist(trainingLevel2);
    assertThrows(
        InvalidDataAccessApiUsageException.class, () -> trainingLevelRepository.findById(null));
  }

  @Test
  public void findAll() {
    List<TrainingLevel> expectedTrainingLevel = Arrays.asList(trainingLevel1, trainingLevel2);
    expectedTrainingLevel.stream().forEach(g -> entityManager.persist(g));
    List<TrainingLevel> resultTrainingLevel = trainingLevelRepository.findAll();
    assertNotNull(resultTrainingLevel);
    assertEquals(expectedTrainingLevel, resultTrainingLevel);
    assertEquals(2, resultTrainingLevel.size());
  }

  @Test
  public void findAllEmptyDatabase() {
    List<TrainingLevel> expectedTrainingLevel = new ArrayList<>();
    List<TrainingLevel> resultTrainingLevel = trainingLevelRepository.findAll();
    assertNotNull(resultTrainingLevel);
    assertEquals(expectedTrainingLevel, resultTrainingLevel);
    assertEquals(0, resultTrainingLevel.size());
  }

  @Test
  public void findAllByTrainingDefinition() {
    entityManager.persist(trainingLevel1);
    entityManager.persist(trainingLevel2);
    entityManager.persist(trainingLevel3);
    List<TrainingLevel> resultTrainingLevels =
        trainingLevelRepository.findAllByTrainingDefinitionId(trainingDefinition1.getId());
    assertTrue(
        resultTrainingLevels.stream().anyMatch(l -> l.getId().equals(trainingLevel1.getId())));
    assertTrue(
        resultTrainingLevels.stream().anyMatch(l -> l.getId().equals(trainingLevel3.getId())));
    assertEquals(2, resultTrainingLevels.size());
  }
}
