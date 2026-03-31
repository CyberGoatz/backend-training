package cz.cyberrange.platform.training.rest.controllers;

import static cz.cyberrange.platform.training.rest.controllers.util.ObjectConverter.convertJsonBytesToObject;
import static cz.cyberrange.platform.training.rest.controllers.util.ObjectConverter.convertObjectToJsonBytes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.querydsl.core.types.Predicate;
import cz.cyberrange.platform.training.api.dto.UserRefDTO;
import cz.cyberrange.platform.training.api.dto.traininginstance.TrainingInstanceAssignPoolIdDTO;
import cz.cyberrange.platform.training.api.dto.traininginstance.TrainingInstanceBasicInfoDTO;
import cz.cyberrange.platform.training.api.dto.traininginstance.TrainingInstanceCreateDTO;
import cz.cyberrange.platform.training.api.dto.traininginstance.TrainingInstanceDTO;
import cz.cyberrange.platform.training.api.dto.traininginstance.TrainingInstanceFindAllResponseDTO;
import cz.cyberrange.platform.training.api.dto.traininginstance.TrainingInstanceUpdateDTO;
import cz.cyberrange.platform.training.api.exceptions.EntityNotFoundException;
import cz.cyberrange.platform.training.api.exceptions.MicroserviceApiException;
import cz.cyberrange.platform.training.api.exceptions.errors.JavaApiError;
import cz.cyberrange.platform.training.api.responses.PageResultResource;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.persistence.util.TestDataFactory;
import cz.cyberrange.platform.training.rest.utils.error.ApiError;
import cz.cyberrange.platform.training.rest.utils.error.CustomRestExceptionHandlerTraining;
import cz.cyberrange.platform.training.service.facade.TrainingInstanceFacade;
import cz.cyberrange.platform.training.service.mapping.mapstruct.BetaTestingGroupMapperImpl;
import cz.cyberrange.platform.training.service.mapping.mapstruct.TrainingDefinitionMapperImpl;
import cz.cyberrange.platform.training.service.mapping.mapstruct.TrainingInstanceMapper;
import cz.cyberrange.platform.training.service.mapping.mapstruct.TrainingInstanceMapperImpl;
import cz.cyberrange.platform.training.service.mapping.mapstruct.UserRefMapperImpl;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.querydsl.binding.QuerydslBindingsFactory;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.querydsl.QuerydslPredicateArgumentResolver;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@SpringBootTest(
    classes = {
      TestDataFactory.class,
      TrainingInstanceMapperImpl.class,
      TrainingDefinitionMapperImpl.class,
      UserRefMapperImpl.class,
      BetaTestingGroupMapperImpl.class
    })
public class TrainingInstancesRestControllerTest {

  private TrainingInstancesRestController trainingInstancesRestController;

  @Autowired private TestDataFactory testDataFactory;
  @Autowired TrainingInstanceMapper trainingInstanceMapper;

  @MockBean private TrainingInstanceFacade trainingInstanceFacade;

  private MockMvc mockMvc;
  private AutoCloseable closeable;

  private TrainingInstance trainingInstance1, trainingInstance2;
  private TrainingInstanceDTO trainingInstance1DTO, trainingInstance2DTO;
  private TrainingInstanceCreateDTO trainingInstanceCreateDTO;
  private TrainingInstanceUpdateDTO trainingInstanceUpdateDTO;
  private UserRefDTO organizerDTO1, organizerDTO2, organizerDTO3;
  private TrainingInstanceAssignPoolIdDTO trainingInstanceAssignPoolIdDTO;
  private TrainingInstanceBasicInfoDTO trainingInstanceBasicInfoDTO;
  private PageResultResource.Pagination pagination;

  private Page page;

  private PageResultResource<TrainingInstanceFindAllResponseDTO>
      trainingInstanceDTOPageResultResource;

  @BeforeEach
  public void init() throws Exception {
    ObjectMapper snakeCaseMapper = new ObjectMapper();
    snakeCaseMapper.setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());

    closeable = MockitoAnnotations.openMocks(this);
    trainingInstancesRestController =
        new TrainingInstancesRestController(trainingInstanceFacade, snakeCaseMapper);
    this.mockMvc =
        MockMvcBuilders.standaloneSetup(trainingInstancesRestController)
            .setCustomArgumentResolvers(
                new PageableHandlerMethodArgumentResolver(),
                new QuerydslPredicateArgumentResolver(
                    new QuerydslBindingsFactory(SimpleEntityPathResolver.INSTANCE),
                    Optional.empty()))
            .setMessageConverters(new MappingJackson2HttpMessageConverter(snakeCaseMapper))
            .setControllerAdvice(new CustomRestExceptionHandlerTraining())
            .build();

    organizerDTO1 = testDataFactory.getUserRefDTO1();
    organizerDTO2 = testDataFactory.getUserRefDTO2();
    organizerDTO3 = testDataFactory.getUserRefDTO3();

    trainingInstance1 = testDataFactory.getConcludedInstance();
    trainingInstance1.setId(1L);

    trainingInstance2 = testDataFactory.getOngoingInstance();
    trainingInstance2.setId(2L);

    trainingInstance1DTO = testDataFactory.getTrainingInstanceDTO();
    trainingInstance1DTO.setId(1L);

    trainingInstance2DTO = testDataFactory.getTrainingInstanceDTO();
    trainingInstance2DTO.setId(2L);
    trainingInstance2DTO.setTitle("DTO2");

    trainingInstanceCreateDTO = testDataFactory.getTrainingInstanceCreateDTO();
    trainingInstanceCreateDTO.setTrainingDefinitionId(1L);

    trainingInstanceUpdateDTO = testDataFactory.getTrainingInstanceUpdateDTO();
    trainingInstanceUpdateDTO.setId(5L);
    trainingInstanceUpdateDTO.setTrainingDefinitionId(1L);

    trainingInstanceAssignPoolIdDTO = new TrainingInstanceAssignPoolIdDTO();
    trainingInstanceAssignPoolIdDTO.setPoolId(1L);

    trainingInstanceBasicInfoDTO = new TrainingInstanceBasicInfoDTO();

    page = new PageImpl<>(List.of(trainingInstance1, trainingInstance2));

    trainingInstanceDTOPageResultResource = trainingInstanceMapper.mapToPageResultResource(page);
  }

  @AfterEach
  void closeService() throws Exception {
    closeable.close();
  }

  @Test
  public void findTrainingInstanceById() throws Exception {
    given(trainingInstanceFacade.findById(any(Long.class))).willReturn(trainingInstance1DTO);
    MockHttpServletResponse result =
        mockMvc
            .perform(get("/training-instances" + "/{id}", 1L))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andReturn()
            .getResponse();
    assertEquals(
        trainingInstance1DTO,
        convertJsonBytesToObject(result.getContentAsString(), TrainingInstanceDTO.class));
  }

  @Test
  public void findTrainingInstanceById_NotFound() throws Exception {
    willThrow(new EntityNotFoundException())
        .given(trainingInstanceFacade)
        .findById(any(Long.class));
    MockHttpServletResponse response =
        mockMvc
            .perform(get("/training-instances" + "/{id}", 6l))
            .andExpect(status().isNotFound())
            .andReturn()
            .getResponse();
    ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
    assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
    assertEquals("The requested entity could not be found", error.getMessage());
  }

  @Test
  public void findAllTrainingInstances() throws Exception {
    given(trainingInstanceFacade.findAll(any(Predicate.class), any(Pageable.class)))
        .willReturn(trainingInstanceDTOPageResultResource);
    MockHttpServletResponse result =
        mockMvc
            .perform(get("/training-instances"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andReturn()
            .getResponse();
    assertEquals(
        convertObjectToJsonBytes(convertObjectToJsonBytes(trainingInstanceDTOPageResultResource)),
        result.getContentAsString());
  }

  @Test
  public void createTrainingInstances() throws Exception {
    given(trainingInstanceFacade.create(any(TrainingInstanceCreateDTO.class)))
        .willReturn(trainingInstance1DTO);
    MockHttpServletResponse result =
        mockMvc
            .perform(
                post("/training-instances")
                    .content(convertObjectToJsonBytes(trainingInstanceCreateDTO))
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse();
    assertEquals(
        convertObjectToJsonBytes(convertObjectToJsonBytes(trainingInstance1DTO)),
        result.getContentAsString());
  }

  @Test
  public void createTrainingInstances_FacadeException() throws Exception {
    willThrow(new EntityNotFoundException())
        .given(trainingInstanceFacade)
        .create(any(TrainingInstanceCreateDTO.class));
    MockHttpServletResponse result =
        mockMvc
            .perform(
                post("/training-instances")
                    .content(convertObjectToJsonBytes(trainingInstanceCreateDTO))
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isNotFound())
            .andReturn()
            .getResponse();
    ApiError error = convertJsonBytesToObject(result.getContentAsString(), ApiError.class);
    assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
    assertEquals("The requested entity could not be found", error.getMessage());
  }

  @Test
  public void updateTrainingInstances() throws Exception {
    given(trainingInstanceFacade.update(any(TrainingInstanceUpdateDTO.class)))
        .willReturn("new token");
    MockHttpServletResponse result =
        mockMvc
            .perform(
                put("/training-instances")
                    .content(convertObjectToJsonBytes(trainingInstanceUpdateDTO))
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse();
    assertEquals("new token", convertJsonBytesToObject(result.getContentAsString()));
  }

  @Test
  public void updateTrainingInstances_FacadeException() throws Exception {
    willThrow(new EntityNotFoundException())
        .given(trainingInstanceFacade)
        .update(any(TrainingInstanceUpdateDTO.class));
    MockHttpServletResponse result =
        mockMvc
            .perform(
                put("/training-instances")
                    .content(convertObjectToJsonBytes(trainingInstanceUpdateDTO))
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isNotFound())
            .andReturn()
            .getResponse();
    ApiError error = convertJsonBytesToObject(result.getContentAsString(), ApiError.class);
    assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
    assertEquals("The requested entity could not be found", error.getMessage());
  }

  @Test
  public void deleteTrainingInstance() throws Exception {
    mockMvc
        .perform(
            delete("/training-instances" + "/{id}", 1l)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk());
  }

  @Test
  public void deleteTrainingInstance_FacadeException() throws Exception {
    willThrow(new EntityNotFoundException())
        .given(trainingInstanceFacade)
        .delete(anyLong(), anyBoolean());
    MockHttpServletResponse result =
        mockMvc
            .perform(
                delete("/training-instances" + "/{id}", 1L)
                    .queryParam("forceDelete", "true")
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isNotFound())
            .andReturn()
            .getResponse();
    ApiError error = convertJsonBytesToObject(result.getContentAsString(), ApiError.class);
    assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
    assertEquals("The requested entity could not be found", error.getMessage());
  }

  @Test
  public void assignPool() throws Exception {
    given(trainingInstanceFacade.assignPoolToTrainingInstance(1L, trainingInstanceAssignPoolIdDTO))
        .willReturn(trainingInstanceBasicInfoDTO);
    mockMvc
        .perform(
            patch("/training-instances" + "/{instanceId}/" + "assign-pool", 1L)
                .content(convertObjectToJsonBytes(trainingInstanceAssignPoolIdDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk());
  }

  @Test
  public void assignPool_FacadeException() throws Exception {
    willThrow(new EntityNotFoundException())
        .given(trainingInstanceFacade)
        .assignPoolToTrainingInstance(anyLong(), any(TrainingInstanceAssignPoolIdDTO.class));
    MockHttpServletResponse response =
        mockMvc
            .perform(
                patch("/training-instances" + "/{instanceId}/" + "assign-pool", 698L)
                    .content(convertObjectToJsonBytes(trainingInstanceAssignPoolIdDTO))
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isNotFound())
            .andReturn()
            .getResponse();
    ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
    assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
    assertEquals("The requested entity could not be found", error.getMessage());
  }

  @Test
  public void unassignPool() throws Exception {
    mockMvc
        .perform(
            patch("/training-instances" + "/{instanceId}/unassign-pool", 1L)
                .content(convertObjectToJsonBytes(trainingInstanceAssignPoolIdDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk());
  }

  @Test
  public void unassignPool_FacadeException() throws Exception {
    willThrow(new MicroserviceApiException(HttpStatus.FORBIDDEN, JavaApiError.of("Detail")))
        .given(trainingInstanceFacade)
        .unassignPoolInTrainingInstance(anyLong());
    MockHttpServletResponse response =
        mockMvc
            .perform(
                patch("/training-instances" + "/{instanceId}/unassign-pool", 1L)
                    .content(convertObjectToJsonBytes(trainingInstanceAssignPoolIdDTO))
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isForbidden())
            .andReturn()
            .getResponse();
    ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
    assertEquals(HttpStatus.FORBIDDEN, error.getStatus());
  }

  @Test
  public void getOrganizersOfGivenTrainingInstance() throws Exception {
    PageResultResource<UserRefDTO> expectedUsersRefDTOs =
        new PageResultResource<>(List.of(organizerDTO1, organizerDTO2), pagination);
    pagination = new PageResultResource.Pagination(0, 2, 5, 2, 1);
    given(
            trainingInstanceFacade.getOrganizersOfTrainingInstance(
                eq(trainingInstance1.getId()), any(Pageable.class), eq(null), eq(null)))
        .willReturn(expectedUsersRefDTOs);

    MockHttpServletResponse result =
        mockMvc
            .perform(get("/training-instances/{id}/organizers", trainingInstance1.getId()))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse();
    assertEquals(
        expectedUsersRefDTOs.getContent(),
        convertJsonBytesToObject(
                convertJsonBytesToObject(result.getContentAsString()),
                new TypeReference<PageResultResource<UserRefDTO>>() {})
            .getContent());
  }

  @Test
  public void getOrganizerOfGivenTrainingInstance_FacadeException() throws Exception {
    willThrow(new EntityNotFoundException())
        .given(trainingInstanceFacade)
        .getOrganizersOfTrainingInstance(
            eq(trainingInstance1.getId()), any(Pageable.class), eq(null), eq(null));
    MockHttpServletResponse response =
        mockMvc
            .perform(get("/training-instances/{id}/organizers", trainingInstance1.getId()))
            .andExpect(status().isNotFound())
            .andReturn()
            .getResponse();
    ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
    assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
    assertEquals("The requested entity could not be found", error.getMessage());
  }

  @Test
  public void getOrganizersNotInGivenTrainingInstance() throws Exception {
    PageResultResource<UserRefDTO> expectedUsersRefDTOs =
        new PageResultResource<>(List.of(organizerDTO1, organizerDTO2), pagination);
    pagination = new PageResultResource.Pagination(0, 2, 5, 2, 1);
    given(
            trainingInstanceFacade.getOrganizersNotInGivenTrainingInstance(
                eq(trainingInstance1.getId()), any(Pageable.class), eq(null), eq(null)))
        .willReturn(expectedUsersRefDTOs);

    MockHttpServletResponse result =
        mockMvc
            .perform(
                get(
                    "/training-instances/{id}/organizers-not-in-training-instance",
                    trainingInstance1.getId()))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse();
    assertEquals(
        expectedUsersRefDTOs.getContent(),
        convertJsonBytesToObject(
                convertJsonBytesToObject(result.getContentAsString()),
                new TypeReference<PageResultResource<UserRefDTO>>() {})
            .getContent());
  }

  @Test
  public void getOrganizersNotInGivenTrainingInstance_FacadeException() throws Exception {
    willThrow(new EntityNotFoundException())
        .given(trainingInstanceFacade)
        .getOrganizersNotInGivenTrainingInstance(
            eq(trainingInstance1.getId()), any(Pageable.class), eq(null), eq(null));
    MockHttpServletResponse response =
        mockMvc
            .perform(
                get(
                    "/training-instances/{id}/organizers-not-in-training-instance",
                    trainingInstance1.getId()))
            .andExpect(status().isNotFound())
            .andReturn()
            .getResponse();
    ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
    assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
    assertEquals("The requested entity could not be found", error.getMessage());
  }

  @Test
  public void editOrganizers() throws Exception {
    mockMvc
        .perform(
            put("/training-instances/{id}/organizers", trainingInstance1.getId())
                .param("organizersAddition", "1,2,3")
                .param("organizersRemoval", "3,4"))
        .andExpect(status().isNoContent());
    then(trainingInstanceFacade)
        .should()
        .editOrganizers(trainingInstance1.getId(), Set.of(1L, 2L, 3L), Set.of(3L, 4L));
  }

  @Test
  public void editOrganizers_FacadeException() throws Exception {
    willThrow(
            new MicroserviceApiException(
                "Unexpected error when calling user and group service.",
                HttpStatus.FORBIDDEN,
                JavaApiError.of("Detail")))
        .given(trainingInstanceFacade)
        .editOrganizers(trainingInstance1.getId(), Set.of(1L, 2L), Set.of(4L));
    MockHttpServletResponse response =
        mockMvc
            .perform(
                put("/training-instances/{id}/organizers", trainingInstance1.getId())
                    .param("organizersAddition", "1,2")
                    .param("organizersRemoval", "4"))
            .andExpect(status().isForbidden())
            .andReturn()
            .getResponse();
    ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
    assertEquals(HttpStatus.FORBIDDEN, error.getStatus());
    assertEquals(
        "Unexpected error when calling user and group service. Detail", error.getMessage());
  }
}
