package cz.cyberrange.platform.training.persistence.repository;

import static org.junit.jupiter.api.Assertions.*;

import cz.cyberrange.platform.training.persistence.model.TrainingDefinition;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.persistence.util.TestDataFactory;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@DataJpaTest
public class TrainingInstanceRepositoryTest {

  @Autowired private TestEntityManager entityManager;
  @Autowired private TestDataFactory testDataFactory;
  @Autowired private TrainingInstanceRepository trainingInstanceRepository;

  private TrainingInstance trainingInstance1, trainingInstance2;
  private TrainingDefinition trainingDefinition;

  @BeforeEach
  public void setUp() {
    trainingDefinition = testDataFactory.getReleasedDefinition();

    trainingInstance1 = testDataFactory.getOngoingInstance();
    trainingInstance1.setTrainingDefinition(entityManager.persist(trainingDefinition));

    trainingInstance2 = testDataFactory.getConcludedInstance();
    trainingInstance2.setTrainingDefinition(entityManager.persist(trainingDefinition));
  }

  @Test
  public void findById() {
    Long id = (Long) entityManager.persistAndGetId(trainingInstance1);
    Optional<TrainingInstance> optionalTrainingInstance = trainingInstanceRepository.findById(id);
    assertTrue(optionalTrainingInstance.isPresent());
    assertEquals(trainingInstance1, optionalTrainingInstance.get());
  }

  @Test
  public void findByIdNotInTheDatabase() {
    Optional<TrainingInstance> optionalTrainingInstance = trainingInstanceRepository.findById(5L);
    assertFalse(optionalTrainingInstance.isPresent());
  }

  @Test
  public void findAll() {
    entityManager.persist(trainingInstance1);
    entityManager.persist(trainingInstance2);
    List<TrainingInstance> resultTrainingInstance = trainingInstanceRepository.findAll();
    assertNotNull(resultTrainingInstance);
    assertEquals(2, resultTrainingInstance.size());
    assertTrue(resultTrainingInstance.contains(trainingInstance1));
    assertTrue(resultTrainingInstance.contains(trainingInstance2));
  }

  @Test
  public void findAllEmptyDatabase() {
    List<TrainingInstance> expectedTrainingInstances = new ArrayList<>();
    List<TrainingInstance> resultTrainingInstances = trainingInstanceRepository.findAll();
    assertNotNull(resultTrainingInstances);
    assertEquals(expectedTrainingInstances.size(), resultTrainingInstances.size());
    assertEquals(expectedTrainingInstances, resultTrainingInstances);
  }

  @Test
  public void isFinishedTestReturnTrue() {
    TrainingInstance ti = entityManager.persist(trainingInstance2);
    assertTrue(
        trainingInstanceRepository.isFinished(ti.getId(), LocalDateTime.now(Clock.systemUTC())));
  }

  @Test
  public void isFinishedTestReturnFalse() {
    TrainingInstance ti = entityManager.persist(trainingInstance1);
    assertFalse(
        trainingInstanceRepository.isFinished(ti.getId(), LocalDateTime.now(Clock.systemUTC())));
  }

  @Test
  public void findAllInstancesByTrainingDefinitionId() {
    entityManager.persist(trainingInstance1);
    entityManager.persist(trainingInstance2);

    List<TrainingInstance> instances =
        trainingInstanceRepository.findAllByTrainingDefinitionId(trainingDefinition.getId());
    assertTrue(instances.contains(trainingInstance1));
    assertTrue(instances.contains(trainingInstance2));
  }

  @Test
  public void findAllPaginationTest() {
    entityManager.persist(trainingInstance1);
    entityManager.persist(trainingInstance2);

    Pageable pageable = PageRequest.of(0, 1);
    Page<TrainingInstance> trainingInstances = trainingInstanceRepository.findAll(pageable);
    assertEquals(1, trainingInstances.getContent().size());

    pageable = PageRequest.of(1, 1);
    trainingInstances = trainingInstanceRepository.findAll(pageable);
    assertEquals(1, trainingInstances.getContent().size());
  }

  @Test
  public void existsToken() {
    entityManager.persist(trainingInstance1);
    assertTrue(trainingInstanceRepository.existsForToken(trainingInstance1.getAccessToken()));

    assertFalse(trainingInstanceRepository.existsForToken("non-existing-token"));
  }
}
