package cz.cyberrange.platform.training.persistence.model;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import cz.cyberrange.platform.training.persistence.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
public class InfoLevelEntityTest {

  @Autowired private TestDataFactory testDataFactory;
  @Autowired private TestEntityManager entityManager;

  private InfoLevel infoLevel;

  @BeforeEach
  public void init() {
    infoLevel = testDataFactory.getInfoLevel1();
  }

  @Test
  public void saveShouldPersistData() {
    InfoLevel iL = this.entityManager.persistFlushFind(infoLevel);
    assertNotNull(iL.getId());
  }
}
