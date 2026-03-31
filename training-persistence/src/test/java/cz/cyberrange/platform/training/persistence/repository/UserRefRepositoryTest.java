package cz.cyberrange.platform.training.persistence.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cz.cyberrange.platform.training.persistence.model.UserRef;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
public class UserRefRepositoryTest {

  @Autowired private TestEntityManager entityManager;

  @Autowired private UserRefRepository userRefRepository;

  private UserRef userRef1, userRef2, userRef3;

  @BeforeEach
  public void setUp() {
    userRef1 = new UserRef();
    userRef1.setUserRefId(1L);

    userRef2 = new UserRef();
    userRef2.setUserRefId(2L);

    userRef3 = new UserRef();
    userRef3.setUserRefId(3L);

    entityManager.persist(userRef1);
    entityManager.persist(userRef2);
    entityManager.persist(userRef3);
  }

  @Test
  public void findAllByLoggedInUser() {
    Set<UserRef> users =
        userRefRepository.findUsers(Set.of(userRef1.getUserRefId(), userRef3.getUserRefId()));
    assertEquals(2, users.size());
    assertTrue(users.contains(userRef1));
    assertTrue(users.contains(userRef3));
  }
}
