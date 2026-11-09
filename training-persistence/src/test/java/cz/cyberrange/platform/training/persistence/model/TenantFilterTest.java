package cz.cyberrange.platform.training.persistence.model;

import cz.cyberrange.platform.training.persistence.model.enums.TDState;
import cz.cyberrange.platform.training.persistence.repository.TrainingDefinitionRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingInstanceRepository;
import cz.cyberrange.platform.training.persistence.util.TestDataFactory;
import org.hibernate.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class TenantFilterTest {

    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private TestDataFactory testDataFactory;
    @Autowired
    private TrainingDefinitionRepository trainingDefinitionRepository;
    @Autowired
    private TrainingInstanceRepository trainingInstanceRepository;

    private TrainingDefinition globalDefinition;
    private TrainingDefinition vitDefinition;
    private TrainingDefinition otherDefinition;
    private TrainingInstance vitInstance;
    private TrainingInstance otherInstance;

    @BeforeEach
    void setUp() {
        globalDefinition = entityManager.persist(definition(null));
        vitDefinition = entityManager.persist(definition("vit"));
        otherDefinition = entityManager.persist(definition("other"));

        vitInstance = testDataFactory.getOngoingInstance();
        vitInstance.setTrainingDefinition(globalDefinition);
        vitInstance.setTenantKey("vit");
        vitInstance.setAllowedGroupId(42L);
        vitInstance = entityManager.persist(vitInstance);

        otherInstance = testDataFactory.getConcludedInstance();
        otherInstance.setTrainingDefinition(otherDefinition);
        otherInstance.setTenantKey("other");
        otherInstance = entityManager.persist(otherInstance);
        entityManager.flush();
        entityManager.clear();
    }

    @AfterEach
    void disableFilter() {
        session().disableFilter(TenantFilter.NAME);
    }

    @Test
    void withoutFilterEveryRowIsReturned() {
        assertEquals(3, trainingDefinitionRepository.findAllByState(TDState.RELEASED, PageRequest.of(0, 10)).getTotalElements());
        assertEquals(2, trainingInstanceRepository.findAll().size());
    }

    @Test
    void filterKeepsOwnAndGlobalDefinitions() {
        enableFilter("vit");
        Set<Long> ids = trainingDefinitionRepository.findAllByState(TDState.RELEASED, PageRequest.of(0, 10)).getContent().stream()
                .map(TrainingDefinition::getId)
                .collect(Collectors.toSet());
        assertEquals(Set.of(globalDefinition.getId(), vitDefinition.getId()), ids);
    }

    @Test
    void filterKeepsOwnInstancesOnly() {
        enableFilter("vit");
        List<TrainingInstance> instances = trainingInstanceRepository.findAll();
        assertEquals(1, instances.size());
        assertEquals(vitInstance.getId(), instances.get(0).getId());
        assertEquals(42L, instances.get(0).getAllowedGroupId());
    }

    @Test
    void filterDoesNotApplyToLoadsById() {
        enableFilter("vit");
        assertTrue(trainingInstanceRepository.findById(otherInstance.getId()).isPresent(),
                "loads by id bypass Hibernate filters, so services must check the tenant explicitly");
    }

    @Test
    void tenantKeyIsNotUpdatable() {
        TrainingDefinition detached = trainingDefinitionRepository.findById(vitDefinition.getId()).orElseThrow();
        entityManager.detach(detached);
        detached.setTenantKey("other");
        trainingDefinitionRepository.saveAndFlush(detached);
        entityManager.clear();
        assertEquals("vit", trainingDefinitionRepository.findById(vitDefinition.getId()).orElseThrow().getTenantKey());
    }

    private TrainingDefinition definition(String tenantKey) {
        TrainingDefinition definition = testDataFactory.getReleasedDefinition();
        definition.setTenantKey(tenantKey);
        return definition;
    }

    private void enableFilter(String tenantKey) {
        session().enableFilter(TenantFilter.NAME).setParameter(TenantFilter.PARAM, tenantKey);
    }

    private Session session() {
        return entityManager.getEntityManager().unwrap(Session.class);
    }
}
