package cz.cyberrange.platform.training.persistence.repository;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import cz.cyberrange.platform.training.persistence.model.Hint;
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
public class HintRepositoryTest {

  @Autowired private TestEntityManager entityManager;
  @Autowired private TestDataFactory testDataFactory;
  @Autowired private HintRepository hintRepository;

  private Hint hint1, hint2;

  @BeforeEach
  public void init() {
    hint1 = testDataFactory.getHint1();
    hint2 = testDataFactory.getHint2();
  }

  @Test
  public void findById() throws Exception {
    long id = entityManager.persist(hint1).getId();
    Optional<Hint> hintOptional = hintRepository.findById(id);
    assertThat(hintOptional.isPresent());
    assertEquals(hint1, hintOptional.get());
  }

  @Test
  public void findByIdNullableArgument() throws Exception {
    entityManager.persist(hint1);
    assertThrows(InvalidDataAccessApiUsageException.class, () -> hintRepository.findById(null));
  }

  @Test
  public void findAll() {
    List<Hint> expectedHints = Arrays.asList(hint1, hint2);
    expectedHints.stream().forEach(h -> entityManager.persist(h));
    List<Hint> resultHints = hintRepository.findAll();
    assertNotNull(resultHints);
    assertEquals(expectedHints, resultHints);
    assertEquals(2, resultHints.size());
  }

  @Test
  public void findAllEmptyDatabase() {
    List<Hint> expectedHints = new ArrayList<>();
    List<Hint> resultHints = hintRepository.findAll();
    assertNotNull(resultHints);
    assertEquals(expectedHints, resultHints);
    assertEquals(0, resultHints.size());
  }
}
