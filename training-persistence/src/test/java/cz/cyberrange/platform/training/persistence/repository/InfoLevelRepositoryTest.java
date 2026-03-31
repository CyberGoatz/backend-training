package cz.cyberrange.platform.training.persistence.repository;

import static org.junit.jupiter.api.Assertions.*;

import cz.cyberrange.platform.training.persistence.model.InfoLevel;
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
public class InfoLevelRepositoryTest {

  @Autowired private TestEntityManager entityManager;
  @Autowired private TestDataFactory testDataFactory;
  @Autowired private InfoLevelRepository infoLevelRepository;

  private InfoLevel infoLevel, infoLevel2;

  @BeforeEach
  public void init() {
    infoLevel = testDataFactory.getInfoLevel1();
    infoLevel2 = testDataFactory.getInfoLevel2();
  }

  @Test
  public void findById() throws Exception {
    long expectedId = entityManager.persist(infoLevel).getId();
    Optional<InfoLevel> infoLevelOptional = infoLevelRepository.findById(expectedId);
    InfoLevel iL =
        infoLevelOptional.orElseThrow(() -> new Exception("Training run should be found"));
    assertNotNull(iL.getId());
    assertEquals(infoLevel.getContent(), iL.getContent());
  }

  @Test
  public void findById_IdNotInTheDatabase() {
    Optional<InfoLevel> infoLevelOptional = infoLevelRepository.findById(2L);
    assertFalse(infoLevelOptional.isPresent());
  }

  @Test
  public void findAll() {
    List<InfoLevel> expectedInfoLevels = Arrays.asList(infoLevel, infoLevel2);
    expectedInfoLevels.stream().forEach(i -> entityManager.persist(i));
    List<InfoLevel> resultInfoLevels = infoLevelRepository.findAll();
    assertNotNull(resultInfoLevels);
    assertEquals(expectedInfoLevels, resultInfoLevels);
    assertEquals(expectedInfoLevels.size(), resultInfoLevels.size());
  }

  @Test
  public void findAll_emptyDatabase() {
    List<InfoLevel> expectedInfoLevels = new ArrayList<>();
    List<InfoLevel> resultInfoLevels = infoLevelRepository.findAll();
    assertNotNull(resultInfoLevels);
    assertEquals(expectedInfoLevels.size(), resultInfoLevels.size());
    assertEquals(expectedInfoLevels, resultInfoLevels);
  }
}
