package cz.cyberrange.platform.training.persistence.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cz.cyberrange.platform.training.persistence.model.BetaTestingGroup;
import cz.cyberrange.platform.training.persistence.model.InfoLevel;
import cz.cyberrange.platform.training.persistence.model.TrainingDefinition;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import cz.cyberrange.platform.training.persistence.model.UserRef;
import cz.cyberrange.platform.training.persistence.util.TestDataFactory;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@DataJpaTest
public class TrainingDefinitionRepositoryTest {

  @Autowired private TestEntityManager entityManager;
  @Autowired private TestDataFactory testDataFactory;
  @Autowired private TrainingDefinitionRepository trainingDefinitionRepository;

  private TrainingDefinition trainingDefinitionWithBG1,
      trainingDefinitionWithBG2,
      trainingDefinitionWithBG3,
      trainingDefinitionWithBG4;
  private TrainingDefinition trainingDefinition1, trainingDefinition2;
  private UserRef author1, author2, organizer1;
  private BetaTestingGroup viewGroup1, viewGroup2, viewGroup3, viewGroup4;
  private Pageable pageable;

  @BeforeEach
  public void init() {
    author1 = new UserRef();
    author1.setUserRefId(3L);
    author2 = new UserRef();
    author2.setUserRefId(8L);
    organizer1 = new UserRef();
    organizer1.setUserRefId(5L);

    trainingDefinition1 = testDataFactory.getReleasedDefinition();
    trainingDefinition2 = testDataFactory.getUnreleasedDefinition();

    trainingDefinitionWithBG1 = testDataFactory.getReleasedDefinition();
    trainingDefinitionWithBG2 = testDataFactory.getReleasedDefinition();
    trainingDefinitionWithBG3 = testDataFactory.getReleasedDefinition();
    trainingDefinitionWithBG4 = testDataFactory.getReleasedDefinition();
    viewGroup1 = new BetaTestingGroup();
    viewGroup2 = new BetaTestingGroup();
    viewGroup3 = new BetaTestingGroup();
    viewGroup4 = new BetaTestingGroup();

    trainingDefinitionWithBG1.setBetaTestingGroup(viewGroup1);
    trainingDefinitionWithBG2.setBetaTestingGroup(viewGroup2);
    trainingDefinitionWithBG3.setBetaTestingGroup(viewGroup3);
    trainingDefinitionWithBG4.setBetaTestingGroup(viewGroup4);

    entityManager.persist(trainingDefinition1);
    entityManager.persist(trainingDefinition2);
    entityManager.persist(trainingDefinitionWithBG1);
    entityManager.persist(trainingDefinitionWithBG2);
    entityManager.persist(trainingDefinitionWithBG3);
    entityManager.persist(trainingDefinitionWithBG4);

    pageable = PageRequest.of(0, 10);
  }

  @Test
  public void findAllAsDesigner() {
    entityManager.persist(author1);
    entityManager.persist(author2);
    entityManager.persist(organizer1);

    trainingDefinition2.addAuthor(author1);
    trainingDefinition2.addAuthor(author2);
    entityManager.merge(trainingDefinition2);
    trainingDefinitionWithBG1.addAuthor(author1);
    viewGroup1.addOrganizer(organizer1);
    entityManager.merge(trainingDefinitionWithBG2);
    trainingDefinitionWithBG2.addAuthor(author1);
    trainingDefinitionWithBG2.addAuthor(author2);
    viewGroup2.addOrganizer(author1);
    viewGroup2.addOrganizer(organizer1);
    entityManager.merge(trainingDefinitionWithBG2);

    trainingDefinitionWithBG3.addAuthor(author2);
    viewGroup3.addOrganizer(author2);
    entityManager.merge(trainingDefinitionWithBG3);

    trainingDefinitionWithBG4.addAuthor(author2);
    viewGroup4.addOrganizer(author1);
    viewGroup4.addOrganizer(organizer1);
    entityManager.merge(trainingDefinitionWithBG4);

    List<TrainingDefinition> trainingDefinitions =
        trainingDefinitionRepository
            .findAll(null, PageRequest.of(0, 10), author1.getUserRefId())
            .getContent();
    assertTrue(
        trainingDefinitions.containsAll(
            List.of(
                trainingDefinition2,
                trainingDefinitionWithBG1,
                trainingDefinitionWithBG2,
                trainingDefinitionWithBG4)));
    assertEquals(4, trainingDefinitions.size());
  }

  @Test
  public void findAllPlayedByUser() {
    InfoLevel infoLevel1 = testDataFactory.getInfoLevel1();
    InfoLevel infoLevel2 = testDataFactory.getInfoLevel2();
    InfoLevel infoLevel3 = testDataFactory.getInfoLevel1();

    infoLevel1.setTrainingDefinition(trainingDefinition1);
    infoLevel2.setTrainingDefinition(trainingDefinition2);
    infoLevel3.setTrainingDefinition(trainingDefinitionWithBG3);

    entityManager.persist(infoLevel1);
    entityManager.persist(infoLevel2);
    entityManager.persist(infoLevel3);

    UserRef participantRef1 = testDataFactory.getUserRef1();
    UserRef participantRef2 = testDataFactory.getUserRef2();
    entityManager.persist(participantRef1);
    entityManager.persist(participantRef2);

    TrainingInstance trainingInstance1 = testDataFactory.getConcludedInstance();
    TrainingInstance trainingInstance2 = testDataFactory.getOngoingInstance();
    TrainingInstance trainingInstance3 = testDataFactory.getFutureInstance();

    trainingInstance1.setTrainingDefinition(trainingDefinition1);
    trainingInstance2.setTrainingDefinition(trainingDefinition2);
    trainingInstance3.setTrainingDefinition(trainingDefinitionWithBG3);

    entityManager.persist(trainingInstance1);
    entityManager.persist(trainingInstance2);
    entityManager.persist(trainingInstance3);

    TrainingRun trainingRun1 = testDataFactory.getRunningRun();
    trainingRun1.setParticipantRef(participantRef1);
    trainingRun1.setTrainingInstance(trainingInstance1);
    trainingRun1.setCurrentLevel(infoLevel1);

    TrainingRun trainingRun2 = testDataFactory.getArchivedRun();
    trainingRun2.setParticipantRef(participantRef1);
    trainingRun2.setTrainingInstance(trainingInstance2);
    trainingRun2.setCurrentLevel(infoLevel2);

    TrainingRun trainingRun3 = testDataFactory.getFinishedRun();
    trainingRun3.setParticipantRef(participantRef2);
    trainingRun3.setTrainingInstance(trainingInstance1);
    trainingRun3.setCurrentLevel(infoLevel1);

    TrainingRun trainingRun4 = testDataFactory.getRunningRun();
    trainingRun4.setParticipantRef(participantRef2);
    trainingRun4.setTrainingInstance(trainingInstance3);
    trainingRun4.setCurrentLevel(infoLevel3);

    entityManager.persist(trainingRun1);
    entityManager.persist(trainingRun2);
    entityManager.persist(trainingRun3);
    entityManager.persist(trainingRun4);

    List<TrainingDefinition> trainingDefinitions =
        trainingDefinitionRepository.findAllPlayedByUser(participantRef1.getUserRefId());
    assertTrue(trainingDefinitions.containsAll(List.of(trainingDefinition1, trainingDefinition2)));
    assertEquals(2, trainingDefinitions.size());
  }
}
