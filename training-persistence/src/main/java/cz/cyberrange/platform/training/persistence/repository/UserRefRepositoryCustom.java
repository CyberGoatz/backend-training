package cz.cyberrange.platform.training.persistence.repository;

import cz.cyberrange.platform.training.persistence.model.UserRef;
import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

public interface UserRefRepositoryCustom {

  /**
   * Insert user reference if it does not exist in the database.
   *
   * @param userRefId the user reference id
   * @return the number of rows affected
   */
  @Modifying
  @Transactional
  UserRef createOrGet(@Param("userRefId") Long userRefId);
}
