package cz.cyberrange.platform.training.persistence.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import cz.cyberrange.platform.training.persistence.model.enums.TRState;
import cz.cyberrange.platform.training.persistence.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
public class TrainingRunEntityTest {

  @Autowired private TestDataFactory testDataFactory;
  @Autowired private TestEntityManager entityManager;

  private TrainingRun trainingRun1, trainingRun2;
  private TrainingInstance trainingInstance;
  private TrainingDefinition trainingDefinition;
  private InfoLevel infoLevel;
  private UserRef participantRef;

  @BeforeEach
  public void init() {
    trainingInstance = testDataFactory.getOngoingInstance();
    trainingDefinition = testDataFactory.getReleasedDefinition();
    infoLevel = testDataFactory.getInfoLevel1();

    participantRef = new UserRef();
    participantRef.setUserRefId(3L);
  }

  @Test
  public void saveShouldPersistData() {
    trainingRun1 = testDataFactory.getRunningRun();
    trainingRun1.setCurrentLevel(entityManager.persist(infoLevel));
    trainingRun1.setParticipantRef(entityManager.persist(participantRef));
    trainingRun1.setTrainingInstance(entityManager.persist(trainingInstance));

    TrainingRun tr = this.entityManager.persistFlushFind(trainingRun1);
    assertEquals(TRState.RUNNING, tr.getState());
  }
}
