package cz.cyberrange.platform.training.rest.controllers;

import static cz.cyberrange.platform.training.rest.controllers.util.ObjectConverter.convertJsonBytesToObject;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import cz.cyberrange.platform.training.api.converters.LocalDateTimeDeserializer;
import cz.cyberrange.platform.training.api.dto.archive.TrainingInstanceArchiveDTO;
import cz.cyberrange.platform.training.api.dto.export.FileToReturnDTO;
import cz.cyberrange.platform.training.api.dto.imports.AssessmentLevelImportDTO;
import cz.cyberrange.platform.training.api.dto.imports.ImportTrainingDefinitionDTO;
import cz.cyberrange.platform.training.api.dto.imports.InfoLevelImportDTO;
import cz.cyberrange.platform.training.api.dto.imports.TrainingLevelImportDTO;
import cz.cyberrange.platform.training.api.exceptions.EntityNotFoundException;
import cz.cyberrange.platform.training.persistence.util.TestDataFactory;
import cz.cyberrange.platform.training.rest.utils.error.ApiError;
import cz.cyberrange.platform.training.rest.utils.error.CustomRestExceptionHandlerTraining;
import cz.cyberrange.platform.training.service.facade.ExportImportFacade;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.querydsl.binding.QuerydslBindingsFactory;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.querydsl.QuerydslPredicateArgumentResolver;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@SpringBootTest(classes = TestDataFactory.class)
public class ExportImportRestControllerTest {

  private ExportImportRestController exportImportRestController;
  @Autowired private TestDataFactory testDataFactory;
  @MockBean private ObjectMapper objectMapper;
  @MockBean private ExportImportFacade exportImportFacade;

  private MockMvc mockMvc;
  private AutoCloseable closeable;

  private TrainingInstanceArchiveDTO trainingInstanceArchiveDTO;
  private ImportTrainingDefinitionDTO importTrainingDefinitionDTO;

  @BeforeEach
  public void init() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());
    objectMapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);

    closeable = MockitoAnnotations.openMocks(this);
    exportImportRestController = new ExportImportRestController(exportImportFacade, objectMapper);
    this.mockMvc =
        MockMvcBuilders.standaloneSetup(exportImportRestController)
            .setCustomArgumentResolvers(
                new PageableHandlerMethodArgumentResolver(),
                new QuerydslPredicateArgumentResolver(
                    new QuerydslBindingsFactory(SimpleEntityPathResolver.INSTANCE),
                    Optional.empty()))
            .setMessageConverters(
                new MappingJackson2HttpMessageConverter(objectMapper),
                new ByteArrayHttpMessageConverter())
            .setControllerAdvice(new CustomRestExceptionHandlerTraining())
            .build();

    trainingInstanceArchiveDTO = testDataFactory.getTrainingInstanceArchiveDTO();

    InfoLevelImportDTO infoLevelImportDTO = testDataFactory.getInfoLevelImportDTO();
    infoLevelImportDTO.setOrder(0);

    AssessmentLevelImportDTO assessmentLevelDTO = testDataFactory.getAssessmentLevelImportDTO();
    assessmentLevelDTO.setOrder(1);

    TrainingLevelImportDTO trainingLevelImportDTO = testDataFactory.getTrainingLevelImportDTO();
    trainingLevelImportDTO.setOrder(2);

    importTrainingDefinitionDTO = testDataFactory.getImportTrainingDefinitionDTO();
    importTrainingDefinitionDTO.setLevels(
        Arrays.asList(infoLevelImportDTO, assessmentLevelDTO, trainingLevelImportDTO));
  }

  @AfterEach
  void closeService() throws Exception {
    closeable.close();
  }

  @Test
  public void archiveTrainingInstance() throws Exception {
    FileToReturnDTO file = new FileToReturnDTO();
    file.setContent(convertObjectToJsonBytes(trainingInstanceArchiveDTO).getBytes());
    file.setTitle(trainingInstanceArchiveDTO.getTitle());
    String valueTi = convertObjectToJsonBytes(trainingInstanceArchiveDTO);
    given(objectMapper.writeValueAsString(any(Object.class))).willReturn(valueTi);
    given(exportImportFacade.archiveTrainingInstance(any(Long.class))).willReturn(file);
    mockMvc
        .perform(get("/exports/training-instances" + "/{id}", 1L))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_OCTET_STREAM));
  }

  @Test
  public void archiveTrainingInstanceWithFacadeException() throws Exception {
    willThrow(new EntityNotFoundException())
        .given(exportImportFacade)
        .archiveTrainingInstance(any(Long.class));
    MockHttpServletResponse response =
        mockMvc
            .perform(get("/exports/training-instances" + "/{id}", 600l))
            .andExpect(status().isNotFound())
            .andReturn()
            .getResponse();
    ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
    assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
    assertEquals("The requested entity could not be found", error.getMessage());
  }

  @Test
  public void importTrainingDefinition() throws Exception {
    mockMvc
        .perform(
            post("/imports/training-definitions")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(convertObjectToJsonBytes(importTrainingDefinitionDTO)))
        .andExpect(status().isOk());
  }

  private static String convertObjectToJsonBytes(Object object) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());
    mapper.registerModule(
        new JavaTimeModule().addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer()));
    return mapper.writeValueAsString(object);
  }
}
