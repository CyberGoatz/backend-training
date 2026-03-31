package cz.cyberrange.platform.training.persistence.repository;

import cz.cyberrange.platform.training.persistence.model.UserRef;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** The JPA repository interface to manage {@link UserRef} instances. */
@Repository
public interface UserRefRepository
    extends JpaRepository<UserRef, Long>,
        QuerydslPredicateExecutor<UserRef>,
        UserRefRepositoryCustom {

  /**
   * Find all users by userRefIds.
   *
   * @param userRefId the user ref id
   * @return the set of {@link UserRef}
   */
  Set<UserRef> findUsers(@Param("userRefId") Set<Long> userRefId);

  /**
   * Find user by user ref id.
   *
   * @param userRefId the user id
   * @return the {@link UserRef}
   */
  Optional<UserRef> findUserByUserRefId(@Param("userRefId") Long userRefId);

  /**
   * Find all participants ref ids of given training instance.
   *
   * @param trainingInstanceId id of the training instance
   * @return the ids of the participants
   */
  Set<Long> findParticipantsRefIdsByTrainingInstanceId(
      @Param("trainingInstanceId") Long trainingInstanceId);
}
