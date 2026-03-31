package cz.cyberrange.platform.training.service.services;

import cz.cyberrange.platform.training.api.dto.UserRefDTO;
import cz.cyberrange.platform.training.api.enums.RoleType;
import cz.cyberrange.platform.training.api.exceptions.CustomWebClientException;
import cz.cyberrange.platform.training.api.exceptions.EntityErrorDetail;
import cz.cyberrange.platform.training.api.exceptions.EntityNotFoundException;
import cz.cyberrange.platform.training.api.exceptions.MicroserviceApiException;
import cz.cyberrange.platform.training.api.responses.PageResultResource;
import cz.cyberrange.platform.training.persistence.model.UserRef;
import cz.cyberrange.platform.training.persistence.repository.UserRefRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

/** The type User service. */
@Service
public class UserService {

  private final WebClient userManagementServiceWebClient;
  private final UserRefRepository userRefRepository;

  /**
   * Instantiates a new User service.
   *
   * @param userManagementServiceWebClient the rest template
   * @param userRefRepository the user ref repository
   */
  public UserService(
      @Qualifier("userManagementServiceWebClient") WebClient userManagementServiceWebClient,
      UserRefRepository userRefRepository) {
    this.userManagementServiceWebClient = userManagementServiceWebClient;
    this.userRefRepository = userRefRepository;
  }

  /**
   * Finds specific User reference by login
   *
   * @param userRefId of wanted User reference
   * @return {@link UserRef} with corresponding login
   * @throws EntityNotFoundException UserRef was not found
   */
  public UserRef getUserByUserRefId(Long userRefId) {
    return userRefRepository
        .findUserByUserRefId(userRefId)
        .orElseThrow(
            () ->
                new EntityNotFoundException(
                    new EntityErrorDetail(UserRef.class, "id", userRefId.getClass(), userRefId)));
  }

  /**
   * If user reference with given user id does not exist, it is created and returned. Otherwise, the
   * existing one is returned.
   *
   * @param userRefId id of the referenced user
   * @return user reference with given referenced id
   */
  public UserRef createOrGetUserRef(Long userRefId) {
    return userRefRepository.createOrGet(userRefId);
  }

  /**
   * Finds specific User reference by login
   *
   * @param id of wanted User reference
   * @return {@link UserRef} with corresponding login
   * @throws EntityNotFoundException UserRef was not found
   */
  public UserRefDTO getUserRefDTOByUserRefId(Long id) {
    try {
      return userManagementServiceWebClient
          .get()
          .uri("/users/{id}", id)
          .retrieve()
          .bodyToMono(UserRefDTO.class)
          .block();
    } catch (CustomWebClientException ex) {
      throw new MicroserviceApiException(
          "Error when calling user management service API to obtain info about user(ID: "
              + id
              + ").",
          ex);
    }
  }

  /**
   * Gets users with given user ref ids.
   *
   * @param userRefIds the user ref ids
   * @param pageable pageable parameter with information about pagination.
   * @param givenName optional parameter used for filtration
   * @param familyName optional parameter used for filtration
   * @return the users with given user ref ids
   */
  public PageResultResource<UserRefDTO> getUsersRefDTOByGivenUserIds(
      List<Long> userRefIds, Pageable pageable, String givenName, String familyName) {
    if (userRefIds.isEmpty()) {
      return new PageResultResource<>(
          Collections.emptyList(),
          new PageResultResource.Pagination(0, 0, pageable.getPageSize(), 0, 0));
    }
    try {
      return userManagementServiceWebClient
          .get()
          .uri(
              uriBuilder -> {
                uriBuilder
                    .path("/users/ids")
                    .queryParam("ids", StringUtils.collectionToDelimitedString(userRefIds, ","));
                this.setCommonParams(givenName, familyName, pageable, uriBuilder);
                return uriBuilder.build();
              })
          .retrieve()
          .bodyToMono(new ParameterizedTypeReference<PageResultResource<UserRefDTO>>() {})
          .block();
    } catch (CustomWebClientException ex) {
      throw new MicroserviceApiException(
          "Error when calling user management service API to obtain users by IDs: "
              + userRefIds
              + ".",
          ex);
    }
  }

  public List<UserRefDTO> getUsersRefDTOByGivenUserIds(List<Long> participantsRefIds) {
    List<UserRefDTO> participants = new ArrayList<>();
    PageResultResource<UserRefDTO> participantsInfo;
    int page = 0;
    do {
      participantsInfo =
          this.getUsersRefDTOByGivenUserIds(
              participantsRefIds, PageRequest.of(page, 999), null, null);
      participants.addAll(participantsInfo.getContent());
      page++;
    } while (page < participantsInfo.getPagination().getTotalPages());
    return participants;
  }

  /**
   * Finds all logins of users that have role of designer
   *
   * @param roleType the role type
   * @param pageable pageable parameter with information about pagination.
   * @param givenName optional parameter used for filtration
   * @param familyName optional parameter used for filtration
   * @return list of users with given role
   */
  public PageResultResource<UserRefDTO> getUsersByGivenRole(
      RoleType roleType, Pageable pageable, String givenName, String familyName) {
    try {
      return userManagementServiceWebClient
          .get()
          .uri(
              uriBuilder -> {
                uriBuilder.path("/roles/users").queryParam("roleType", roleType.name());
                this.setCommonParams(givenName, familyName, pageable, uriBuilder);
                return uriBuilder.build();
              })
          .retrieve()
          .bodyToMono(new ParameterizedTypeReference<PageResultResource<UserRefDTO>>() {})
          .block();
    } catch (CustomWebClientException ex) {
      throw new MicroserviceApiException(
          "Error when calling user management service API to obtain users with role "
              + roleType.name()
              + ".",
          ex);
    }
  }

  /**
   * Finds all logins of users that have role of designer
   *
   * @param roleType the role type
   * @param userRefIds ids of the users who should be excluded from the result set.
   * @param pageable the pageable
   * @param givenName optional parameter used for filtration
   * @param familyName optional parameter used for filtration
   * @return list of users with given role
   */
  public PageResultResource<UserRefDTO> getUsersByGivenRoleAndNotWithGivenIds(
      RoleType roleType,
      Set<Long> userRefIds,
      Pageable pageable,
      String givenName,
      String familyName) {
    try {
      return userManagementServiceWebClient
          .get()
          .uri(
              uriBuilder -> {
                uriBuilder
                    .path("/roles/users-not-with-ids")
                    .queryParam("roleType", roleType.name())
                    .queryParam("ids", StringUtils.collectionToDelimitedString(userRefIds, ","));
                this.setCommonParams(givenName, familyName, pageable, uriBuilder);
                return uriBuilder.build();
              })
          .retrieve()
          .bodyToMono(new ParameterizedTypeReference<PageResultResource<UserRefDTO>>() {})
          .block();
    } catch (CustomWebClientException ex) {
      throw new MicroserviceApiException(
          "Error when calling user management service API to obtain users with role "
              + roleType.name()
              + " and IDs: "
              + userRefIds
              + ".",
          ex);
    }
  }

  /**
   * Gets user ref id from user and group.
   *
   * @return the user ref id from user and group
   */
  public UserRefDTO getUserRefFromUserAndGroup() {
    try {
      return userManagementServiceWebClient
          .get()
          .uri("/users/info")
          .retrieve()
          .bodyToMono(UserRefDTO.class)
          .block();
    } catch (CustomWebClientException ex) {
      throw new MicroserviceApiException(
          "Error when calling user management service API to get info about logged in user.", ex);
    }
  }

  private void setCommonParams(
      String givenName, String familyName, Pageable pageable, UriBuilder builder) {
    if (givenName != null) {
      builder.queryParam("givenName", givenName);
    }
    if (familyName != null) {
      builder.queryParam("familyName", familyName);
    }
    builder.queryParam("page", pageable.getPageNumber());
    builder.queryParam("size", pageable.getPageSize());
  }
}
