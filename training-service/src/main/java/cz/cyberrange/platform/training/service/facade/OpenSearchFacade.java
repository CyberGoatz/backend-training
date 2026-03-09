package cz.cyberrange.platform.training.service.facade;

import com.fasterxml.jackson.databind.JsonNode;
import cz.cyberrange.platform.training.opensearch.logging.exceptions.OpenSearchQueryException;
import cz.cyberrange.platform.training.opensearch.logging.exceptions.OpenSearchSerializeException;
import cz.cyberrange.platform.training.opensearch.querying.OpenSearchSqlService;
import cz.cyberrange.platform.training.persistence.model.UserRef;
import cz.cyberrange.platform.training.persistence.repository.TrainingInstanceRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingRunRepository;
import cz.cyberrange.platform.training.service.enums.RoleTypeSecurity;
import cz.cyberrange.platform.training.service.services.SecurityService;
import cz.cyberrange.platform.training.service.services.TrainingInstanceService;
import cz.cyberrange.platform.training.service.services.TrainingRunService;
import cz.cyberrange.platform.training.service.services.UserService;
import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
public class OpenSearchFacade {

  private final OpenSearchSqlService opensearchSqlService;
  private final SecurityService securityService;
  private final TrainingRunRepository trainingRunRepository;
  private final UserService userService;

  @Autowired
  public OpenSearchFacade(
      OpenSearchSqlService opensearchSqlService,
      SecurityService securityService,
      TrainingRunRepository trainingRunRepository,
      UserService userService) {
    this.opensearchSqlService = opensearchSqlService;
    this.securityService = securityService;
    this.trainingRunRepository = trainingRunRepository;
    this.userService = userService;
  }

  public JsonNode handleSqlQuery(@NonNull String query) throws OpenSearchQueryException, OpenSearchSerializeException {
    if (securityService.hasRole(RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR)) {
      return opensearchSqlService.executeSqlQueryFromAdmin(query);
    }

    List<Long> allowedInstanceIds = this.getUserTrainingInstanceIds();
    List<Long> allowedRunIds = this.getUserTrainingRunIds();
    return opensearchSqlService.executeSqlQueryWithAccessControl(
        query, allowedInstanceIds, allowedRunIds);
  }

  public List<Long> getUserTrainingRunIds() {
    Long user = securityService.getUserRefIdFromUserAndGroup();
    return trainingRunRepository.findAllByParticipantRefId(user).stream()
        .map(trainingRun -> trainingRun.getId())
        .toList();
  }

  public List<Long> getUserTrainingInstanceIds() {
    Long userId = securityService.getUserRefIdFromUserAndGroup();
    UserRef userRef = userService.getUserByUserRefId(userId);
    return userRef.getTrainingInstances().stream()
        .map(trainingInstance -> trainingInstance.getId())
        .toList();
  }
}
