package cz.cyberrange.platform.training.service.tenancy;

import cz.cyberrange.platform.training.api.dto.UserRefDTO;
import cz.cyberrange.platform.training.api.exceptions.EntityConflictException;
import cz.cyberrange.platform.training.api.exceptions.EntityNotFoundException;
import cz.cyberrange.platform.training.api.exceptions.ForbiddenException;
import cz.cyberrange.platform.training.persistence.model.TrainingDefinition;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.persistence.model.UserRef;
import cz.cyberrange.platform.training.persistence.model.enums.TDState;
import cz.cyberrange.platform.training.persistence.repository.*;
import cz.cyberrange.platform.training.service.mapping.mapstruct.CloneMapper;
import cz.cyberrange.platform.training.service.services.AuditEventsService;
import cz.cyberrange.platform.training.service.services.SecurityService;
import cz.cyberrange.platform.training.service.services.TrainingDefinitionService;
import cz.cyberrange.platform.training.service.services.TrainingInstanceService;
import cz.cyberrange.platform.training.service.services.TrainingRunService;
import cz.cyberrange.platform.training.service.services.UserService;
import cz.cyberrange.platform.training.service.services.api.AnswersStorageApiService;
import cz.cyberrange.platform.training.service.services.api.ElasticsearchApiService;
import cz.cyberrange.platform.training.service.services.api.SandboxApiService;
import cz.cyberrange.platform.training.service.startup.DefaultLevelsLoader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static cz.cyberrange.platform.training.service.tenancy.TenantTestSupport.signIn;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The visibility rule as applied by the training services: tenant callers read their own rows and the global
 * library, cannot change the global library, never see other tenants' rows, and callers without the claim are
 * unaffected.
 */
class TenantVisibilityServicesTest {

    private final TrainingDefinitionRepository definitionRepository = mock(TrainingDefinitionRepository.class);
    private final TrainingInstanceRepository instanceRepository = mock(TrainingInstanceRepository.class);
    private final UserRefRepository userRefRepository = mock(UserRefRepository.class);
    private final SecurityService securityService = mock(SecurityService.class);
    private final UserService userService = mock(UserService.class);

    private TrainingDefinitionService definitionService;
    private TrainingInstanceService instanceService;
    private TrainingRunService runService;

    @BeforeEach
    void setUp() {
        definitionService = new TrainingDefinitionService(definitionRepository, mock(AbstractLevelRepository.class),
                mock(InfoLevelRepository.class), mock(TrainingLevelRepository.class), mock(AssessmentLevelRepository.class),
                mock(AccessLevelRepository.class), instanceRepository, mock(MitreTechniqueRepository.class),
                mock(HintRepository.class), userRefRepository, securityService, userService, mock(DefaultLevelsLoader.class),
                mock(CloneMapper.class));
        instanceService = new TrainingInstanceService(instanceRepository, mock(TrainingRunRepository.class), userRefRepository,
                securityService, userService);
        runService = new TrainingRunService(mock(TrainingRunRepository.class), mock(AbstractLevelRepository.class), instanceRepository,
                userRefRepository, mock(HintRepository.class), mock(AuditEventsService.class), mock(ElasticsearchApiService.class),
                mock(AnswersStorageApiService.class), securityService, mock(QuestionAnswerRepository.class),
                mock(SandboxApiService.class), mock(TRAcquisitionLockRepository.class), mock(SubmissionRepository.class),
                mock(TrainingLevelResultRepository.class));

        UserRef user = new UserRef();
        user.setUserRefId(7L);
        UserRefDTO userRefDTO = new UserRefDTO();
        userRefDTO.setUserRefFullName("Teacher");
        when(securityService.getUserRefIdFromUserAndGroup()).thenReturn(7L);
        when(userRefRepository.createOrGet(anyLong())).thenReturn(user);
        when(userService.getUserRefFromUserAndGroup()).thenReturn(userRefDTO);
        when(definitionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(instanceRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(instanceRepository.existsForToken(anyString())).thenReturn(false);
    }

    @AfterEach
    void tearDown() {
        TenantTestSupport.signOut();
    }

    @Test
    void otherTenantsDefinitionIsNotFound() {
        stubDefinition(1L, "other", TDState.RELEASED);
        signIn(List.of("vit"));
        assertThrows(EntityNotFoundException.class, () -> definitionService.findById(1L));
    }

    @Test
    void ownAndGlobalDefinitionsAreReadable() {
        TrainingDefinition own = stubDefinition(1L, "vit", TDState.UNRELEASED);
        TrainingDefinition global = stubDefinition(2L, null, TDState.RELEASED);
        signIn(List.of("vit"));
        assertSame(own, definitionService.findById(1L));
        assertSame(global, definitionService.findById(2L));
    }

    @Test
    void callersWithoutClaimReadEverything() {
        TrainingDefinition tenantRow = stubDefinition(1L, "other", TDState.RELEASED);
        signIn(null);
        assertSame(tenantRow, definitionService.findById(1L));
    }

    @Test
    void globalDefinitionsAreReadOnlyForTenants() {
        stubDefinition(2L, null, TDState.UNRELEASED);
        signIn(List.of("vit"));
        TrainingDefinition update = definition(2L, null, TDState.UNRELEASED);
        assertThrows(ForbiddenException.class, () -> definitionService.update(update));
        assertThrows(ForbiddenException.class, () -> definitionService.delete(2L));
        assertThrows(ForbiddenException.class,
                () -> definitionService.switchState(2L, cz.cyberrange.platform.training.api.enums.TDState.RELEASED));
    }

    @Test
    void createdDefinitionBelongsToTheCallersTenantWhateverTheBodySays() {
        signIn(List.of("vit"));
        TrainingDefinition created = definitionService.create(definition(null, "other", TDState.UNRELEASED), false);
        assertEquals("vit", created.getTenantKey());
    }

    @Test
    void updateKeepsTheTenant() {
        stubDefinition(1L, "vit", TDState.UNRELEASED);
        signIn(List.of("vit"));
        TrainingDefinition update = definition(1L, null, TDState.UNRELEASED);
        definitionService.update(update);
        assertEquals("vit", update.getTenantKey());
    }

    @Test
    void otherTenantsInstanceIsNotFound() {
        TrainingInstance instance = instance(5L, "other", null);
        when(instanceRepository.findById(5L)).thenReturn(Optional.of(instance));
        signIn(List.of("vit"));
        assertThrows(EntityNotFoundException.class, () -> instanceService.findById(5L));
    }

    @Test
    void runLookupByAccessTokenHidesOtherTenantsInstances() {
        TrainingInstance instance = instance(5L, "other", null);
        instance.setLocalEnvironment(true);
        when(instanceRepository.findByStartTimeAfterAndEndTimeBeforeAndAccessToken(any(), any())).thenReturn(Optional.of(instance));
        signIn(List.of("vit"));
        assertThrows(EntityNotFoundException.class, () -> runService.getTrainingInstanceForParticularAccessToken("course-1234"));
        signIn(List.of("other"));
        assertSame(instance, runService.getTrainingInstanceForParticularAccessToken("course-1234"));
    }

    @Test
    void restrictedInstanceGetsTenantAndStrongToken() {
        signIn(List.of("vit"));
        TrainingInstance toCreate = instance(null, "other", 12L);
        toCreate.setTrainingDefinition(definition(2L, null, TDState.RELEASED));
        TrainingInstance created = instanceService.create(toCreate);
        assertEquals("vit", created.getTenantKey());
        assertTrue(created.getAccessToken().matches("course-[a-z2-7]{20}"), created.getAccessToken());
    }

    @Test
    void openInstanceKeepsThePin() {
        signIn(null);
        TrainingInstance toCreate = instance(null, null, null);
        toCreate.setTrainingDefinition(definition(2L, null, TDState.RELEASED));
        TrainingInstance created = instanceService.create(toCreate);
        assertNull(created.getTenantKey());
        assertTrue(created.getAccessToken().matches("course-\\d{4}"), created.getAccessToken());
    }

    @Test
    void tenantsCannotBuildInstancesOnUnreleasedGlobalDefinitions() {
        signIn(List.of("vit"));
        TrainingInstance toCreate = instance(null, null, null);
        toCreate.setTrainingDefinition(definition(2L, null, TDState.UNRELEASED));
        assertThrows(EntityConflictException.class, () -> instanceService.create(toCreate));
    }

    @Test
    void updateKeepsTenantAndExistingToken() {
        TrainingInstance persisted = instance(5L, "vit", null);
        persisted.setAccessToken("course-1234");
        persisted.setStartTime(LocalDateTime.now().plusDays(1));
        persisted.setTrainingDefinition(definition(2L, null, TDState.RELEASED));
        when(instanceRepository.findById(5L)).thenReturn(Optional.of(persisted));
        signIn(List.of("vit"));

        TrainingInstance update = instance(5L, null, 12L);
        update.setStartTime(persisted.getStartTime());
        update.setTrainingDefinition(persisted.getTrainingDefinition());
        String token = instanceService.update(update);

        assertEquals("vit", update.getTenantKey());
        assertEquals(12L, update.getAllowedGroupId());
        assertEquals("course-1234", token, "existing instances keep their tokens");
    }

    private TrainingDefinition stubDefinition(Long id, String tenantKey, TDState state) {
        TrainingDefinition definition = definition(id, tenantKey, state);
        when(definitionRepository.findById(id)).thenReturn(Optional.of(definition));
        return definition;
    }

    private static TrainingDefinition definition(Long id, String tenantKey, TDState state) {
        TrainingDefinition definition = new TrainingDefinition();
        definition.setId(id);
        definition.setTitle("Network security");
        definition.setState(state);
        definition.setTenantKey(tenantKey);
        return definition;
    }

    private static TrainingInstance instance(Long id, String tenantKey, Long allowedGroupId) {
        TrainingInstance instance = new TrainingInstance();
        instance.setId(id);
        instance.setTitle("Lab 1");
        instance.setAccessToken("course");
        instance.setStartTime(LocalDateTime.now().plusDays(1));
        instance.setTenantKey(tenantKey);
        instance.setAllowedGroupId(allowedGroupId);
        return instance;
    }
}
