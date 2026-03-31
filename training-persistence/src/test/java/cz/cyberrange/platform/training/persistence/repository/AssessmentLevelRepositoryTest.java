package cz.cyberrange.platform.training.persistence.repository;

import static org.junit.jupiter.api.Assertions.*;

import cz.cyberrange.platform.training.persistence.model.AssessmentLevel;
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

@DataJpaTest
public class AssessmentLevelRepositoryTest {

  @Autowired private TestEntityManager entityManager;
  @Autowired private TestDataFactory testDataFactory;
  @Autowired private AssessmentLevelRepository assessmentLevelRepository;

  private AssessmentLevel assessmentLevel1, assessmentLevel2;

  @BeforeEach
  public void setUp() {
    assessmentLevel1 = testDataFactory.getTest();
    assessmentLevel2 = testDataFactory.getQuestionnaire();
  }

  @Test
  public void findById() {
    Long id = (Long) entityManager.persistAndGetId(assessmentLevel1);
    Optional<AssessmentLevel> optionalAssessmentLevel = assessmentLevelRepository.findById(id);
    assertTrue(optionalAssessmentLevel.isPresent());
    assertEquals(id, optionalAssessmentLevel.get().getId());
    assertEquals(assessmentLevel1, optionalAssessmentLevel.get());
  }

  @Test
  public void findAll() {
    List<AssessmentLevel> extectedParticipantsRef =
        Arrays.asList(assessmentLevel1, assessmentLevel2);
    extectedParticipantsRef.stream().forEach(p -> entityManager.persist(p));
    List<AssessmentLevel> resultParticipantRef = assessmentLevelRepository.findAll();
    assertEquals(extectedParticipantsRef, resultParticipantRef);
    assertEquals(2, resultParticipantRef.size());
  }

  @Test
  public void findAll_emptyDatabase() {
    List<AssessmentLevel> expectedAssesmentLevel = new ArrayList<>();
    List<AssessmentLevel> resultAssesmentLevel = assessmentLevelRepository.findAll();
    assertNotNull(resultAssesmentLevel);
    assertEquals(expectedAssesmentLevel, resultAssesmentLevel);
    assertEquals(0, resultAssesmentLevel.size());
  }
}
