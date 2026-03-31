package cz.cyberrange.platform.training.service.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import cz.cyberrange.platform.training.api.dto.UserRefDTO;
import cz.cyberrange.platform.training.api.enums.RoleType;
import cz.cyberrange.platform.training.api.responses.PageResultResource;
import cz.cyberrange.platform.training.persistence.model.UserRef;
import cz.cyberrange.platform.training.persistence.repository.UserRefRepository;
import cz.cyberrange.platform.training.persistence.util.TestDataFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@SpringBootTest(classes = {TestDataFactory.class})
public class UserServiceTest {

  private UserService userService;
  @MockBean private UserRefRepository userRefRepository;
  @MockBean private ExchangeFunction exchangeFunction;

  private UserRef userRef1, userRef2, userRef3;
  private UserRefDTO userRefDTO1, userRefDTO2;
  private PageResultResource.Pagination pagination;
  private Pageable pageable;

  @BeforeEach
  public void init() {
    MockitoAnnotations.openMocks(this);
    WebClient webClient = WebClient.builder().exchangeFunction(exchangeFunction).build();
    userService = new UserService(webClient, userRefRepository);

    userRef1 = new UserRef();
    userRef1.setId(1L);
    userRef1.setUserRefId(10L);
    userRef2 = new UserRef();
    userRef2.setId(2L);
    userRef2.setUserRefId(20L);
    userRef3 = new UserRef();
    userRef3.setId(3L);
    userRef3.setUserRefId(30L);

    userRefDTO1 =
        createUserRefDTO(
            10L,
            "Bc. Alexander Howell",
            "Howell",
            "Alexander",
            "mail1@test.cz",
            "https://oidc.provider.cz/oidc",
            null);
    userRefDTO2 =
        createUserRefDTO(
            20L,
            "Bc. Peter Reeves",
            "Reeves",
            "Peter",
            "mail2@test.cz",
            "https://oidc.provider.cz/oidc",
            null);

    pageable = PageRequest.of(0, 5);
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

  @Test
  public void getUserByUserRefId() {
    given(userRefRepository.findUserByUserRefId(userRef1.getUserRefId()))
        .willReturn(Optional.of(userRef1));
    UserRef foundUserRef = userService.getUserByUserRefId(userRef1.getUserRefId());
    assertEquals(userRef1, foundUserRef);
  }

  @Test
  public void getUserRefDTOByUserRefId() throws Exception {
    given(exchangeFunction.exchange(any(ClientRequest.class)))
        .willReturn(buildMockResponse(userRefDTO1));
    UserRefDTO foundUserRefDTO = userService.getUserRefDTOByUserRefId(userRef1.getUserRefId());
    assertEquals(userRefDTO1, foundUserRefDTO);
  }

  @Test
  public void getUsersRefDTOByGivenUserIds() throws Exception {
    pagination = new PageResultResource.Pagination(0, 2, 5, 2, 1);
    given(exchangeFunction.exchange(any(ClientRequest.class)))
        .willReturn(
            buildMockResponse(
                new PageResultResource<UserRefDTO>(List.of(userRefDTO1, userRefDTO2), pagination)));

    PageResultResource<UserRefDTO> userRefDTOPageResultResource =
        userService.getUsersRefDTOByGivenUserIds(
            List.of(userRef1.getUserRefId(), userRef2.getUserRefId()), pageable, null, null);
    assertTrue(
        userRefDTOPageResultResource.getContent().containsAll(List.of(userRefDTO1, userRefDTO2)));
    assertEquals(pagination.getNumber(), userRefDTOPageResultResource.getPagination().getNumber());
    assertEquals(
        pagination.getNumberOfElements(),
        userRefDTOPageResultResource.getPagination().getNumberOfElements());
    assertEquals(pagination.getSize(), userRefDTOPageResultResource.getPagination().getSize());
    assertEquals(
        pagination.getTotalElements(),
        userRefDTOPageResultResource.getPagination().getTotalElements());
    assertEquals(
        pagination.getTotalPages(), userRefDTOPageResultResource.getPagination().getTotalPages());
  }

  @Test
  public void getUsersRefDTOByGivenUserIdsWithEmptyIds() {
    pagination = new PageResultResource.Pagination(0, 0, 5, 0, 0);
    PageResultResource<UserRefDTO> userRefDTOPageResultResource =
        userService.getUsersRefDTOByGivenUserIds(new ArrayList<>(), pageable, null, null);
    assertTrue(userRefDTOPageResultResource.getContent().isEmpty());
    assertEquals(pagination.toString(), userRefDTOPageResultResource.getPagination().toString());
  }

  @Test
  public void getUsersByGivenRole() throws Exception {
    pagination = new PageResultResource.Pagination(0, 2, 5, 2, 1);
    given(exchangeFunction.exchange(any(ClientRequest.class)))
        .willReturn(
            buildMockResponse(
                new PageResultResource<UserRefDTO>(List.of(userRefDTO1, userRefDTO2), pagination)));

    PageResultResource<UserRefDTO> userRefDTOPageResultResource =
        userService.getUsersByGivenRole(RoleType.ROLE_TRAINING_DESIGNER, pageable, null, null);
    assertTrue(
        userRefDTOPageResultResource.getContent().containsAll(List.of(userRefDTO1, userRefDTO2)));
  }

  @Test
  public void getUsersByGivenRoleAndNotWithGivenIds() throws Exception {
    pagination = new PageResultResource.Pagination(0, 2, 5, 2, 1);
    given(exchangeFunction.exchange(any(ClientRequest.class)))
        .willReturn(
            buildMockResponse(
                new PageResultResource<UserRefDTO>(List.of(userRefDTO1, userRefDTO2), pagination)));

    PageResultResource<UserRefDTO> userRefDTOPageResultResource =
        userService.getUsersByGivenRoleAndNotWithGivenIds(
            RoleType.ROLE_TRAINING_ORGANIZER,
            Set.of(userRef1.getUserRefId(), userRef2.getUserRefId()),
            pageable,
            null,
            null);
    assertTrue(
        userRefDTOPageResultResource.getContent().containsAll(List.of(userRefDTO1, userRefDTO2)));
    assertEquals(pagination.getNumber(), userRefDTOPageResultResource.getPagination().getNumber());
    assertEquals(
        pagination.getNumberOfElements(),
        userRefDTOPageResultResource.getPagination().getNumberOfElements());
    assertEquals(pagination.getSize(), userRefDTOPageResultResource.getPagination().getSize());
    assertEquals(
        pagination.getTotalElements(),
        userRefDTOPageResultResource.getPagination().getTotalElements());
    assertEquals(
        pagination.getTotalPages(), userRefDTOPageResultResource.getPagination().getTotalPages());
  }

  @Test
  public void createUserRef() {
    UserRef userRef = new UserRef();
    userRef.setUserRefId(userRef1.getUserRefId());
    given(userRefRepository.createOrGet(userRef.getUserRefId())).willReturn(userRef1);
    userService.createOrGetUserRef(userRef.getUserRefId());
    then(userRefRepository).should().createOrGet(userRef.getUserRefId());
  }

  private Mono<ClientResponse> buildMockResponse(Object body) throws IOException {
    ClientResponse clientResponse =
        ClientResponse.create(HttpStatus.OK)
            .body(convertObjectToJsonBytes(body))
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    return Mono.just(clientResponse);
  }

  private static String convertObjectToJsonBytes(Object object) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());
    mapper.registerModule(new JavaTimeModule());
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    mapper.enable(SerializationFeature.INDENT_OUTPUT);
    return mapper.writeValueAsString(object);
  }
}
