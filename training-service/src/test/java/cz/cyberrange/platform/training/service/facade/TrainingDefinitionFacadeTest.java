package cz.cyberrange.platform.training.service.facade;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import cz.cyberrange.platform.training.api.dto.AbstractLevelDTO;
import cz.cyberrange.platform.training.api.dto.BasicLevelInfoDTO;
import cz.cyberrange.platform.training.api.dto.UserRefDTO;
import cz.cyberrange.platform.training.api.dto.assessmentlevel.AssessmentLevelUpdateDTO;
import cz.cyberrange.platform.training.api.dto.infolevel.InfoLevelUpdateDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionByIdDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionCreateDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionUpdateDTO;
import cz.cyberrange.platform.training.api.dto.traininglevel.TrainingLevelUpdateDTO;
import cz.cyberrange.platform.training.api.enums.LevelType;
import cz.cyberrange.platform.training.api.enums.RoleType;
import cz.cyberrange.platform.training.api.enums.TDState;
import cz.cyberrange.platform.training.api.exceptions.EntityConflictException;
import cz.cyberrange.platform.training.api.exceptions.EntityNotFoundException;
import cz.cyberrange.platform.training.api.exceptions.InternalServerErrorException;
import cz.cyberrange.platform.training.api.responses.PageResultResource;
import cz.cyberrange.platform.training.persistence.model.AssessmentLevel;
import cz.cyberrange.platform.training.persistence.model.BetaTestingGroup;
import cz.cyberrange.platform.training.persistence.model.InfoLevel;
import cz.cyberrange.platform.training.persistence.model.TrainingDefinition;
import cz.cyberrange.platform.training.persistence.model.TrainingLevel;
import cz.cyberrange.platform.training.persistence.model.UserRef;
import cz.cyberrange.platform.training.persistence.util.TestDataFactory;
import cz.cyberrange.platform.training.service.enums.RoleTypeSecurity;
import cz.cyberrange.platform.training.service.mapping.mapstruct.AttachmentMapperImpl;
import cz.cyberrange.platform.training.service.mapping.mapstruct.BetaTestingGroupMapperImpl;
import cz.cyberrange.platform.training.service.mapping.mapstruct.HintMapperImpl;
import cz.cyberrange.platform.training.service.mapping.mapstruct.LevelMapperImpl;
import cz.cyberrange.platform.training.service.mapping.mapstruct.MitreTechniqueMapperImpl;
import cz.cyberrange.platform.training.service.mapping.mapstruct.QuestionMapperImpl;
import cz.cyberrange.platform.training.service.mapping.mapstruct.ReferenceSolutionNodeMapperImpl;
import cz.cyberrange.platform.training.service.mapping.mapstruct.TrainingDefinitionMapperImpl;
import cz.cyberrange.platform.training.service.mapping.mapstruct.UserRefMapperImpl;
import cz.cyberrange.platform.training.service.services.SecurityService;
import cz.cyberrange.platform.training.service.services.TrainingDefinitionService;
import cz.cyberrange.platform.training.service.services.UserService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@SpringBootTest(
    classes = {
      TestDataFactory.class,
      TrainingDefinitionMapperImpl.class,
      UserRefMapperImpl.class,
      LevelMapperImpl.class,
      BetaTestingGroupMapperImpl.class,
      QuestionMapperImpl.class,
      HintMapperImpl.class,
      AttachmentMapperImpl.class,
      ReferenceSolutionNodeMapperImpl.class,
      MitreTechniqueMapperImpl.class
    })
public class TrainingDefinitionFacadeTest {

  private TrainingDefinitionFacade trainingDefinitionFacade;

  @Autowired private TestDataFactory testDataFactory;
  @Autowired private TrainingDefinitionMapperImpl trainingDefinitionMapper;
  @Autowired private LevelMapperImpl levelMapper;

  @MockBean private TrainingDefinitionService trainingDefinitionService;
  @MockBean private TrainingFeedbackApiService trainingFeedbackApiService;
  @MockBean private SecurityService securityService;
  @MockBean private UserService userService;

  private TrainingDefinition trainingDefinition1, trainingDefinition2;
  private TrainingDefinitionUpdateDTO trainingDefinitionUpdate;
  private TrainingDefinitionCreateDTO trainingDefinitionCreate;

  private AssessmentLevel assessmentLevel;
  private AssessmentLevelUpdateDTO alUpdate;

  private TrainingLevel trainingLevel;
  private TrainingLevelUpdateDTO gameLevelUpdate;

  private InfoLevel infoLevel;
  private InfoLevelUpdateDTO infoLevelUpdate;

  private UserRef author1, author2, author3;
  private UserRefDTO authorDTO1, authorDTO2, authorDTO3;
  private Pageable pageable;
  private PageResultResource.Pagination pagination;

  @BeforeEach
  public void init() {
    MockitoAnnotations.openMocks(this);
    trainingDefinitionFacade =
        new TrainingDefinitionFacade(
            trainingDefinitionService,
            trainingFeedbackApiService,
            userService,
            securityService,
            trainingDefinitionMapper,
            levelMapper);

    author1 = new UserRef();
    author1.setId(1L);
    author1.setUserRefId(10L);
    author2 = new UserRef();
    author2.setId(2L);
    author2.setUserRefId(20L);
    author3 = new UserRef();
    author3.setId(3L);
    author3.setUserRefId(30L);

    authorDTO1 =
        createUserRefDTO(
            10L,
            "Bc. Alexander Howell",
            "Howell",
            "Alexander",
            "mail1@test.cz",
            "https://oidc.provider.cz/oidc",
            null);
    authorDTO2 =
        createUserRefDTO(
            20L,
            "Bc. Peter Reeves",
            "Reeves",
            "Peter",
            "mail2@test.cz",
            "https://oidc.provider.cz/oidc",
            null);
    authorDTO3 =
        createUserRefDTO(
            30L,
            "Ing. Lee Nicholls",
            "Nicholls",
            "Lee",
            "mail3@test.cz",
            "https://oidc.provider.cz/oidc",
            null);

    trainingLevel = testDataFactory.getPenalizedLevel();
    trainingLevel.setId(2L);
    trainingLevel.setOrder(2);

    gameLevelUpdate = testDataFactory.getTrainingLevelUpdateDTO();
    gameLevelUpdate.setId(2L);

    infoLevel = testDataFactory.getInfoLevel1();
    infoLevel.setId(3L);
    infoLevel.setOrder(3);

    infoLevelUpdate = testDataFactory.getInfoLevelUpdateDTO();
    infoLevelUpdate.setId(3L);

    assessmentLevel = testDataFactory.getQuestionnaire();
    assessmentLevel.setId(1L);
    assessmentLevel.setOrder(1);

    alUpdate = testDataFactory.getAssessmentLevelUpdateDTO();
    alUpdate.setId(2L);

    trainingDefinition1 = testDataFactory.getReleasedDefinition();
    trainingDefinition1.setId(1L);
    trainingDefinition1.setAuthors(new HashSet<>(Set.of(author1, author2)));

    trainingDefinition2 = testDataFactory.getUnreleasedDefinition();
    trainingDefinition2.setId(2L);

    trainingDefinitionUpdate = testDataFactory.getTrainingDefinitionUpdateDTO();
    trainingDefinitionUpdate.setId(1L);

    trainingDefinitionCreate = testDataFactory.getTrainingDefinitionCreateDTO();
    given(userService.getUserRefFromUserAndGroup()).willReturn(authorDTO1);
  }

  @Test
  public void findTrainingDefinitionById() {
    given(trainingDefinitionService.findById(1L)).willReturn(trainingDefinition1);
    given(trainingDefinitionService.findAllLevelsFromDefinition(anyLong()))
        .willReturn(List.of(trainingLevel));
    TrainingDefinitionByIdDTO definition = trainingDefinitionFacade.findById(1L);
    assertEquals(1, definition.getLevels().size());
    AbstractLevelDTO level = definition.getLevels().get(0);
    assertEquals(trainingLevel.getTitle(), level.getTitle());
    assertEquals(trainingLevel.getOrder(), level.getOrder());
    assertEquals(trainingLevel.getId(), level.getId());
    then(trainingDefinitionService).should().findById(1L);
  }

  @Test
  public void findAllTrainingDefinitions() {
    List<TrainingDefinition> expected = new ArrayList<>();
    expected.add(trainingDefinition1);
    expected.add(trainingDefinition2);

    Page<TrainingDefinition> p = new PageImpl<TrainingDefinition>(expected);
    PathBuilder<TrainingDefinition> tD =
        new PathBuilder<TrainingDefinition>(TrainingDefinition.class, "trainingDefinition");
    Predicate predicate = tD.isNotNull();

    given(securityService.hasRole(RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR)).willReturn(true);
    given(trainingDefinitionService.findAll(any(Predicate.class), any(Pageable.class)))
        .willReturn(p);

    PageResultResource<TrainingDefinitionDTO> trainingDefinitionDTO =
        trainingDefinitionFacade.findAll(predicate, PageRequest.of(0, 2));
    deepEquals(trainingDefinition1, trainingDefinitionDTO.getContent().get(0));
    deepEquals(trainingDefinition2, trainingDefinitionDTO.getContent().get(1));

    then(trainingDefinitionService).should().findAll(predicate, PageRequest.of(0, 2));
  }

  @Test
  public void updateTrainingDefinition() {
    BetaTestingGroup viewGroup = new BetaTestingGroup();
    viewGroup.setTrainingDefinition(trainingDefinition1);
    viewGroup.setId(1L);
    viewGroup.setOrganizers(Set.of());
    given(trainingDefinitionService.findById(anyLong())).willReturn(trainingDefinition1);
    trainingDefinitionFacade.update(trainingDefinitionUpdate);
    then(trainingDefinitionService)
        .should()
        .update(trainingDefinitionMapper.mapUpdateToEntity(trainingDefinitionUpdate));
  }

  @Test
  public void updateTrainingDefinitionRemovingBTG() {
    trainingDefinitionUpdate.setBetaTestingGroup(null);
    trainingDefinition1.setBetaTestingGroup(new BetaTestingGroup());
    given(trainingDefinitionService.findById(anyLong())).willReturn(trainingDefinition1);
    assertThrows(
        EntityConflictException.class,
        () -> trainingDefinitionFacade.update(trainingDefinitionUpdate));
  }

  @Test
  public void createTrainingDefinition() {
    given(
            trainingDefinitionService.create(
                trainingDefinitionMapper.mapCreateToEntity(trainingDefinitionCreate), false))
        .willReturn(trainingDefinitionMapper.mapCreateToEntity(trainingDefinitionCreate));
    trainingDefinitionFacade.create(trainingDefinitionCreate);
    then(trainingDefinitionService)
        .should()
        .create(trainingDefinitionMapper.mapCreateToEntity(trainingDefinitionCreate), false);
  }

  @Test
  public void cloneTrainingDefinition() {
    given(trainingDefinitionService.clone(trainingDefinition1.getId(), "title"))
        .willReturn(trainingDefinition1);
    given(trainingDefinitionService.findAllLevelsFromDefinition(anyLong()))
        .willReturn(List.of(trainingLevel));
    TrainingDefinitionByIdDTO definition =
        trainingDefinitionFacade.clone(trainingDefinition1.getId(), "title");
    assertEquals(1, definition.getLevels().size());
    AbstractLevelDTO level = definition.getLevels().get(0);
    assertEquals(trainingLevel.getTitle(), level.getTitle());
    assertEquals(trainingLevel.getOrder(), level.getOrder());
    assertEquals(trainingLevel.getId(), level.getId());
    then(trainingDefinitionService).should().clone(trainingDefinition1.getId(), "title");
  }

  @Test
  public void deleteTrainingDefinition() {
    trainingDefinitionFacade.delete(trainingDefinition1.getId());
    then(trainingDefinitionService).should().delete(trainingDefinition1.getId());
  }

  @Test
  public void deleteOneLevel() {
    trainingDefinitionFacade.deleteOneLevel(trainingDefinition1.getId(), assessmentLevel.getId());
    then(trainingDefinitionService)
        .should()
        .deleteOneLevel(trainingDefinition1.getId(), assessmentLevel.getId());
  }

  @Test
  public void updateAssessmentLevel() {
    assessmentLevel.setTrainingDefinition(trainingDefinition1);
    given(trainingDefinitionService.updateAssessmentLevel(anyLong(), any()))
        .willReturn(assessmentLevel);
    trainingDefinitionFacade.updateAssessmentLevel(trainingDefinition1.getId(), alUpdate);
    then(trainingDefinitionService)
        .should()
        .updateAssessmentLevel(
            trainingDefinition1.getId(), levelMapper.mapUpdateToEntity(alUpdate));
  }

  @Test
  public void updateTrainingLevel() {
    trainingLevel.setTrainingDefinition(trainingDefinition1);
    given(trainingDefinitionService.updateTrainingLevel(anyLong(), any()))
        .willReturn(trainingLevel);
    trainingDefinitionFacade.updateTrainingLevel(trainingDefinition2.getId(), gameLevelUpdate);
    then(trainingDefinitionService)
        .should()
        .updateTrainingLevel(
            trainingDefinition2.getId(), levelMapper.mapUpdateToEntity(gameLevelUpdate));
  }

  @Test
  public void updateInfoLevel() {
    infoLevel.setTrainingDefinition(trainingDefinition1);
    given(trainingDefinitionService.updateInfoLevel(anyLong(), any())).willReturn(infoLevel);
    trainingDefinitionFacade.updateInfoLevel(trainingDefinition2.getId(), infoLevelUpdate);
    then(trainingDefinitionService)
        .should()
        .updateInfoLevel(
            trainingDefinition2.getId(), levelMapper.mapUpdateToEntity(infoLevelUpdate));
  }

  @Test
  public void createInfoLevel() {
    given(trainingDefinitionService.createInfoLevel(trainingDefinition1.getId()))
        .willReturn(infoLevel);
    BasicLevelInfoDTO level = trainingDefinitionFacade.createInfoLevel(trainingDefinition1.getId());
    assertEquals(LevelType.INFO_LEVEL.name(), level.getLevelType().name());
    assertEquals(level.getOrder(), level.getOrder());
    assertEquals(level.getId(), level.getId());
    assertEquals(level.getTitle(), level.getTitle());
    then(trainingDefinitionService).should().createInfoLevel(trainingDefinition1.getId());
  }

  @Test
  public void createGameLevel() {
    given(trainingDefinitionService.createTrainingLevel(trainingDefinition1.getId()))
        .willReturn(trainingLevel);
    BasicLevelInfoDTO level =
        trainingDefinitionFacade.createTrainingLevel(trainingDefinition1.getId());
    assertEquals(LevelType.TRAINING_LEVEL.name(), level.getLevelType().name());
    assertEquals(level.getOrder(), level.getOrder());
    assertEquals(level.getId(), level.getId());
    assertEquals(level.getTitle(), level.getTitle());
    then(trainingDefinitionService).should().createTrainingLevel(trainingDefinition1.getId());
  }

  @Test
  public void createAssessmentLevel() {
    given(trainingDefinitionService.createAssessmentLevel(trainingDefinition1.getId()))
        .willReturn(assessmentLevel);
    BasicLevelInfoDTO level =
        trainingDefinitionFacade.createAssessmentLevel(trainingDefinition1.getId());
    assertEquals(LevelType.ASSESSMENT_LEVEL.name(), level.getLevelType().name());
    assertEquals(level.getOrder(), level.getOrder());
    assertEquals(level.getId(), level.getId());
    assertEquals(level.getTitle(), level.getTitle());
    then(trainingDefinitionService).should().createAssessmentLevel(trainingDefinition1.getId());
  }

  @Test
  public void findLevelByIdGameLevel() {
    given(trainingDefinitionService.findLevelById(trainingLevel.getId())).willReturn(trainingLevel);
    AbstractLevelDTO g = trainingDefinitionFacade.findLevelById(trainingLevel.getId());

    assertEquals(levelMapper.mapToDTO(trainingLevel), g);
    assertEquals(LevelType.TRAINING_LEVEL, g.getLevelType());
  }

  @Test
  public void findLevelByIdInfoLevel() {
    given(trainingDefinitionService.findLevelById(infoLevel.getId())).willReturn(infoLevel);
    AbstractLevelDTO i = trainingDefinitionFacade.findLevelById(infoLevel.getId());

    assertEquals(levelMapper.mapToDTO(infoLevel), i);
    assertEquals(LevelType.INFO_LEVEL, i.getLevelType());
  }

  @Test
  public void findLevelByIdAssessmentLevel() {
    given(trainingDefinitionService.findLevelById(assessmentLevel.getId()))
        .willReturn(assessmentLevel);
    AbstractLevelDTO a = trainingDefinitionFacade.findLevelById(assessmentLevel.getId());

    assertEquals(levelMapper.mapToDTO(assessmentLevel), a);
    assertEquals(LevelType.ASSESSMENT_LEVEL, a.getLevelType());
  }

  @Test
  public void switchState() {
    trainingDefinitionFacade.switchState(trainingDefinition2.getId(), TDState.UNRELEASED);
    then(trainingDefinitionService)
        .should()
        .switchState(trainingDefinition2.getId(), TDState.UNRELEASED);
  }

  @Test
  public void getAuthors() {
    pagination = new PageResultResource.Pagination(0, 2, 5, 2, 1);
    given(trainingDefinitionService.findById(trainingDefinition1.getId()))
        .willReturn(trainingDefinition1);
    given(
            userService.getUsersRefDTOByGivenUserIds(
                trainingDefinition1.getAuthors().stream().map(UserRef::getUserRefId).toList(),
                pageable,
                null,
                null))
        .willReturn(new PageResultResource<>(List.of(authorDTO1, authorDTO2), pagination));
    PageResultResource<UserRefDTO> authors =
        trainingDefinitionFacade.getAuthors(trainingDefinition1.getId(), pageable, null, null);
    assertEquals(authors.getPagination(), pagination);
    assertTrue(authors.getContent().containsAll(Set.of(authorDTO1, authorDTO2)));
  }

  @Test
  public void getAuthorsNotInGivenTrainingDefinition() {
    pagination = new PageResultResource.Pagination(0, 1, 1, 1, 1);
    given(trainingDefinitionService.findById(trainingDefinition1.getId()))
        .willReturn(trainingDefinition1);
    given(
            userService.getUsersByGivenRoleAndNotWithGivenIds(
                RoleType.ROLE_TRAINING_DESIGNER,
                Set.of(author1.getUserRefId(), author2.getUserRefId()),
                pageable,
                null,
                null))
        .willReturn(new PageResultResource<>(List.of(authorDTO3), pagination));
    PageResultResource<UserRefDTO> organizersNotInTrainingInstance =
        trainingDefinitionFacade.getDesignersNotInGivenTrainingDefinition(
            trainingDefinition1.getId(), pageable, null, null);
    assertEquals(pagination.toString(), organizersNotInTrainingInstance.getPagination().toString());
    assertTrue(organizersNotInTrainingInstance.getContent().contains(authorDTO3));
  }

  @Test
  public void getAuthorsNotInGivenTrainingInstanceUserServiceError() {
    given(trainingDefinitionService.findById(trainingDefinition1.getId()))
        .willReturn(trainingDefinition1);
    willThrow(new InternalServerErrorException("Error when calling User And Group endpoint."))
        .given(userService)
        .getUsersByGivenRoleAndNotWithGivenIds(
            RoleType.ROLE_TRAINING_DESIGNER,
            new HashSet<>(Set.of(author1.getUserRefId(), author2.getUserRefId())),
            pageable,
            null,
            null);
    Exception ex =
        assertThrows(
            InternalServerErrorException.class,
            () ->
                trainingDefinitionFacade.getDesignersNotInGivenTrainingDefinition(
                    trainingDefinition1.getId(), pageable, null, null));
    assertEquals("Error when calling User And Group endpoint.", ex.getMessage());
  }

  @Test
  public void editAuthors() {
    PageResultResource.Pagination pagination = new PageResultResource.Pagination(0, 1, 999, 1, 1);
    given(trainingDefinitionService.findById(trainingDefinition1.getId()))
        .willReturn(trainingDefinition1);
    given(securityService.getUserRefIdFromUserAndGroup()).willReturn(author1.getUserRefId());
    given(userService.createOrGetUserRef(author1.getUserRefId())).willReturn(author1);
    given(
            userService.getUsersRefDTOByGivenUserIds(
                List.of(author3.getUserRefId()), PageRequest.of(0, 999), null, null))
        .willReturn(new PageResultResource<>(List.of(authorDTO3), pagination));
    given(userService.createOrGetUserRef(author3.getUserRefId())).willReturn(author3);
    trainingDefinitionFacade.editAuthors(
        trainingDefinition1.getId(),
        new HashSet<>(Set.of(author3.getUserRefId())),
        new HashSet<>(Set.of(author2.getUserRefId())));
    assertEquals(2, trainingDefinition1.getAuthors().size());
    assertTrue(trainingDefinition1.getAuthors().containsAll(Set.of(author1, author3)));
  }

  @Test
  public void editAuthorsRemoveLoggedInAuthor() {
    PageResultResource.Pagination pagination = new PageResultResource.Pagination(0, 1, 999, 1, 1);
    given(trainingDefinitionService.findById(trainingDefinition1.getId()))
        .willReturn(trainingDefinition1);
    given(securityService.getUserRefIdFromUserAndGroup()).willReturn(author1.getUserRefId());
    given(userService.createOrGetUserRef(author1.getUserRefId())).willReturn(author1);
    given(
            userService.getUsersRefDTOByGivenUserIds(
                List.of(author3.getUserRefId()), PageRequest.of(0, 999), null, null))
        .willReturn(new PageResultResource<>(List.of(authorDTO3), pagination));
    given(userService.createOrGetUserRef(author3.getUserRefId())).willReturn(author3);
    trainingDefinitionFacade.editAuthors(
        trainingDefinition1.getId(),
        new HashSet<>(Set.of(author3.getUserRefId())),
        new HashSet<>(Set.of(author1.getUserRefId())));
    assertEquals(3, trainingDefinition1.getAuthors().size());
    assertTrue(trainingDefinition1.getAuthors().containsAll(Set.of(author1, author2, author3)));
  }

  @Test
  public void editAuthorsConcurrentlyRemoveAndAddOAuthorWhoIsNotInTrainingDefinition() {
    PageResultResource.Pagination pagination = new PageResultResource.Pagination(0, 1, 999, 1, 1);
    given(trainingDefinitionService.findById(trainingDefinition1.getId()))
        .willReturn(trainingDefinition1);
    given(securityService.getUserRefIdFromUserAndGroup()).willReturn(author1.getUserRefId());
    given(userService.createOrGetUserRef(author1.getUserRefId())).willReturn(author1);
    given(
            userService.getUsersRefDTOByGivenUserIds(
                List.of(author3.getUserRefId()), PageRequest.of(0, 999), null, null))
        .willReturn(new PageResultResource<>(List.of(authorDTO3), pagination));
    given(userService.createOrGetUserRef(author3.getUserRefId())).willReturn(author3);
    trainingDefinitionFacade.editAuthors(
        trainingDefinition1.getId(),
        new HashSet<>(Set.of(author3.getUserRefId())),
        new HashSet<>(Set.of(author3.getUserRefId())));
    assertEquals(3, trainingDefinition1.getAuthors().size());
    assertTrue(trainingDefinition1.getAuthors().containsAll(Set.of(author1, author2, author3)));
  }

  @Test
  public void editAuthorsConcurrentlyRemoveAndAddAuthorWhoIsInTrainingDefinition() {
    PageResultResource.Pagination pagination = new PageResultResource.Pagination(0, 1, 999, 1, 1);
    given(trainingDefinitionService.findById(trainingDefinition1.getId()))
        .willReturn(trainingDefinition1);
    given(securityService.getUserRefIdFromUserAndGroup()).willReturn(author1.getUserRefId());
    given(userService.createOrGetUserRef(author1.getUserRefId())).willReturn(author1);
    given(
            userService.getUsersRefDTOByGivenUserIds(
                List.of(author2.getUserRefId()), PageRequest.of(0, 999), null, null))
        .willReturn(new PageResultResource<>(List.of(authorDTO2), pagination));
    given(userService.createOrGetUserRef(author2.getUserRefId())).willReturn(author2);
    trainingDefinitionFacade.editAuthors(
        trainingDefinition1.getId(),
        new HashSet<>(Set.of(author2.getUserRefId())),
        new HashSet<>(Set.of(author2.getUserRefId())));
    assertEquals(2, trainingDefinition1.getAuthors().size());
    assertTrue(trainingDefinition1.getAuthors().containsAll(Set.of(author1, author2)));
  }

  @Test
  public void editAuthorsWithEmptySetOfRemovalAndAdditionSets() {
    PageResultResource.Pagination pagination = new PageResultResource.Pagination(0, 1, 999, 1, 1);
    given(trainingDefinitionService.findById(trainingDefinition1.getId()))
        .willReturn(trainingDefinition1);
    given(securityService.getUserRefIdFromUserAndGroup()).willReturn(author1.getUserRefId());
    given(
            userService.getUsersRefDTOByGivenUserIds(
                List.of(author2.getUserRefId()), PageRequest.of(0, 999), null, null))
        .willReturn(new PageResultResource<>(List.of(authorDTO2), pagination));
    given(userService.getUserByUserRefId(author3.getUserRefId())).willReturn(author2);
    trainingDefinitionFacade.editAuthors(
        trainingDefinition1.getId(), new HashSet<>(), new HashSet<>());
    assertEquals(2, trainingDefinition1.getAuthors().size());
    assertTrue(trainingDefinition1.getAuthors().containsAll(Set.of(author1, author2)));
  }

  @Test
  public void editAuthorsRemove() {
    given(trainingDefinitionService.findById(trainingDefinition1.getId()))
        .willReturn(trainingDefinition1);
    given(securityService.getUserRefIdFromUserAndGroup()).willReturn(author1.getUserRefId());
    trainingDefinitionFacade.editAuthors(
        trainingDefinition1.getId(),
        new HashSet<>(),
        new HashSet<>(Set.of(author1.getUserRefId(), author2.getUserRefId())));
    assertEquals(1, trainingDefinition1.getAuthors().size());
    assertTrue(trainingDefinition1.getAuthors().contains(author1));
  }

  @Test
  public void editAuthorsAdd() {
    trainingDefinition1.removeAuthorsByUserRefIds(Set.of(author2.getUserRefId()));
    PageResultResource.Pagination pagination = new PageResultResource.Pagination(0, 1, 999, 1, 1);
    given(trainingDefinitionService.findById(trainingDefinition1.getId()))
        .willReturn(trainingDefinition1);
    given(securityService.getUserRefIdFromUserAndGroup()).willReturn(author1.getUserRefId());
    given(userService.createOrGetUserRef(author1.getUserRefId())).willReturn(author1);
    given(
            userService.getUsersRefDTOByGivenUserIds(
                List.of(author2.getUserRefId(), author3.getUserRefId()),
                PageRequest.of(0, 999),
                null,
                null))
        .willReturn(new PageResultResource<>(List.of(authorDTO3, authorDTO2), pagination));
    given(userService.createOrGetUserRef(author3.getUserRefId())).willReturn(author3);
    given(userService.createOrGetUserRef(author2.getUserRefId())).willReturn(author2);
    trainingDefinitionFacade.editAuthors(
        trainingDefinition1.getId(),
        new HashSet<>(Set.of(author2.getUserRefId(), author3.getUserRefId())),
        new HashSet<>());
    assertEquals(3, trainingDefinition1.getAuthors().size());
    assertTrue(trainingDefinition1.getAuthors().containsAll(Set.of(author1, author2, author3)));
  }

  @Test
  public void editAuthorsAddUserRefNotInDB() {
    trainingDefinition1.removeAuthorsByUserRefIds(Set.of(author2.getUserRefId()));
    PageResultResource.Pagination pagination = new PageResultResource.Pagination(0, 1, 999, 1, 1);
    given(trainingDefinitionService.findById(trainingDefinition1.getId()))
        .willReturn(trainingDefinition1);
    given(securityService.getUserRefIdFromUserAndGroup()).willReturn(author1.getUserRefId());
    given(userService.createOrGetUserRef(author1.getUserRefId())).willReturn(author1);
    given(
            userService.getUsersRefDTOByGivenUserIds(
                List.of(author2.getUserRefId(), author3.getUserRefId()),
                PageRequest.of(0, 999),
                null,
                null))
        .willReturn(new PageResultResource<>(List.of(authorDTO3, authorDTO2), pagination));
    given(userService.createOrGetUserRef(author3.getUserRefId())).willReturn(author3);
    willThrow(EntityNotFoundException.class)
        .given(userService)
        .getUserByUserRefId(author2.getUserRefId());
    given(userService.createOrGetUserRef(author2.getUserRefId())).willReturn(author2);
    trainingDefinitionFacade.editAuthors(
        trainingDefinition1.getId(),
        new HashSet<>(Set.of(author2.getUserRefId(), author3.getUserRefId())),
        new HashSet<>());
    assertEquals(3, trainingDefinition1.getAuthors().size());
    assertTrue(trainingDefinition1.getAuthors().containsAll(Set.of(author1, author2, author3)));
  }

  private UserRefDTO createUserRefDTO(
      Long userRefId,
      String fullName,
      String familyName,
      String givenName,
      String sub,
      String iss,
      byte[] picture) {
    UserRefDTO userRefDTO = new UserRefDTO();
    userRefDTO.setUserRefId(userRefId);
    userRefDTO.setUserRefFullName(fullName);
    userRefDTO.setUserRefFamilyName(familyName);
    userRefDTO.setUserRefGivenName(givenName);
    userRefDTO.setUserRefSub(sub);
    userRefDTO.setIss(iss);
    userRefDTO.setPicture(picture);
    return userRefDTO;
  }

  private void deepEquals(TrainingDefinition expected, TrainingDefinitionDTO actual) {
    assertEquals(expected.getId(), actual.getId());
    assertEquals(expected.getTitle(), actual.getTitle());
    assertEquals(expected.getEstimatedDuration(), actual.getEstimatedDuration());
    assertEquals(expected.getLastEdited(), actual.getLastEdited());
    assertEquals(expected.getDescription(), actual.getDescription());
    assertEquals(expected.getState().name(), actual.getState().name());
  }
}
