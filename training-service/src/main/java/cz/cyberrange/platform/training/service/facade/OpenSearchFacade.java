package cz.cyberrange.platform.training.service.facade;

import cz.cyberrange.platform.training.api.dto.OpenSearchQueryResultDTO;
import cz.cyberrange.platform.training.opensearch.events.training.logging.exceptions.OpenSearchQueryException;
import cz.cyberrange.platform.training.opensearch.events.training.logging.exceptions.OpenSearchSerializeException;
import cz.cyberrange.platform.training.opensearch.sql.OpenSearchSqlService;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import cz.cyberrange.platform.training.persistence.model.UserRef;
import cz.cyberrange.platform.training.persistence.repository.TrainingRunRepository;
import cz.cyberrange.platform.training.service.enums.RoleTypeSecurity;
import cz.cyberrange.platform.training.service.mapping.mapstruct.OpenSearchQueryResultMapper;
import cz.cyberrange.platform.training.service.services.SecurityService;
import cz.cyberrange.platform.training.service.services.UserService;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.stereotype.Service;

@Service
public class OpenSearchFacade {

  private final OpenSearchSqlService opensearchSqlService;
  private final OpenSearchQueryResultMapper openSearchQueryResultMapper;
  private final SecurityService securityService;
  private final TrainingRunRepository trainingRunRepository;
  private final UserService userService;

  @Autowired
  public OpenSearchFacade(
      OpenSearchSqlService opensearchSqlService,
      OpenSearchQueryResultMapper openSearchQueryResultMapper,
      SecurityService securityService,
      TrainingRunRepository trainingRunRepository,
      UserService userService) {
    this.opensearchSqlService = opensearchSqlService;
    this.openSearchQueryResultMapper = openSearchQueryResultMapper;
    this.securityService = securityService;
    this.trainingRunRepository = trainingRunRepository;
    this.userService = userService;
  }

  /**
   * Executes a user-supplied SQL query against OpenSearch, enforcing access control based on the
   * caller's role.
   *
   * <p>Administrators may query any data. Other users are restricted to training instances and runs
   * they are authorised to access.
   *
   * @param query the OpenSearch SQL query to execute
   * @return an {@link OpenSearchQueryResultDTO} containing the page data and pagination metadata
   * @throws OpenSearchQueryException if an error occurs while executing the query
   * @throws OpenSearchSerializeException if the response cannot be parsed
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_TRAINEE)")
  public OpenSearchQueryResultDTO handleSqlQuery(@NonNull String query)
      throws OpenSearchQueryException, OpenSearchSerializeException {
    if (securityService.hasRole(RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR)) {
      return openSearchQueryResultMapper.mapToDTO(
          opensearchSqlService.executeSqlQueryAsAdmin(query));
    }

    if (securityService.hasRole(RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)
        || securityService.hasRole(RoleTypeSecurity.ROLE_TRAINING_TRAINEE)) {
      Set<Long> allowedInstanceIds = this.getUserTrainingInstanceIds();
      Set<Long> allowedRunIds = this.getUserTrainingRunIds(allowedInstanceIds);
      return openSearchQueryResultMapper.mapToDTO(
          opensearchSqlService.executeSqlQueryWithRestrictions(
              query, allowedInstanceIds, allowedRunIds));
    }
    throw new InsufficientAuthenticationException(
        "User does not have sufficient permissions to execute OpenSearch queries.");
  }

  private Set<Long> getUserTrainingRunIds(Set<Long> allowedInstanceIds) {
    Long user = securityService.getUserRefIdFromUserAndGroup();
    return trainingRunRepository.findAllByParticipantRefId(user).stream()
        .filter(run -> allowedInstanceIds.contains(run.getTrainingInstance().getId()))
        .map(TrainingRun::getId)
        .collect(Collectors.toSet());
  }

  private Set<Long> getUserTrainingInstanceIds() {
    Long userId = securityService.getUserRefIdFromUserAndGroup();
    UserRef userRef = userService.getUserByUserRefId(userId);
    return userRef.getTrainingInstances().stream()
        .map(TrainingInstance::getId)
        .collect(Collectors.toSet());
  }
}
