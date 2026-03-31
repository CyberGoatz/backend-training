package cz.cyberrange.platform.training.persistence.repository;

import static org.junit.jupiter.api.Assertions.*;

import cz.cyberrange.platform.training.persistence.model.MitreTechnique;
import cz.cyberrange.platform.training.persistence.util.TestDataFactory;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
public class MitreTechniqueRepositoryTest {

  @Autowired private TestEntityManager entityManager;
  @Autowired private TestDataFactory testDataFactory;
  @Autowired private MitreTechniqueRepository mitreTechniqueRepository;

  private MitreTechnique mitreTechnique1, mitreTechnique2;

  @BeforeEach
  public void setUp() {
    mitreTechnique1 = testDataFactory.getMitreTechnique1();
    mitreTechnique2 = testDataFactory.getMitreTechnique2();
  }

  @Test
  public void findByTechniqueKey() {
    entityManager.persist(mitreTechnique1);
    entityManager.persist(mitreTechnique2);
    Optional<MitreTechnique> optionalMitreTechnique =
        mitreTechniqueRepository.findByTechniqueKey(mitreTechnique1.getTechniqueKey());
    assertTrue(optionalMitreTechnique.isPresent());
    assertEquals(mitreTechnique1, optionalMitreTechnique.get());
  }

  @Test
  public void findAll() {
    entityManager.persist(mitreTechnique1);
    entityManager.persist(mitreTechnique2);
    List<MitreTechnique> resultTechniques = mitreTechniqueRepository.findAll();
    assertNotNull(resultTechniques);
    assertEquals(2, resultTechniques.size());
    assertTrue(resultTechniques.contains(mitreTechnique1));
    assertTrue(resultTechniques.contains(mitreTechnique2));
  }

  @Test
  public void findByTechniqueKeysIn() {
    MitreTechnique mitreTechnique3 = new MitreTechnique();
    mitreTechnique3.setTechniqueKey("T7933.001");
    entityManager.persist(mitreTechnique1);
    entityManager.persist(mitreTechnique2);
    entityManager.persist(mitreTechnique3);

    Set<MitreTechnique> resultTechniques =
        mitreTechniqueRepository.findAllByTechniqueKeyIn(
            Set.of(
                mitreTechnique1.getTechniqueKey(), mitreTechnique3.getTechniqueKey(), "NOT_KEY"));
    assertEquals(2, resultTechniques.size());
    assertTrue(resultTechniques.contains(mitreTechnique1));
    assertTrue(resultTechniques.contains(mitreTechnique3));
  }
}
