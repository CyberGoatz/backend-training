package cz.cyberrange.platform.training.persistence.repository;

import cz.cyberrange.platform.training.persistence.model.TRAcquisitionLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** The JPA repository interface to manage {@link TRAcquisitionLock} instances. */
@Repository
public interface TRAcquisitionLockRepository
    extends JpaRepository<TRAcquisitionLock, Long>,
        QuerydslPredicateExecutor<TRAcquisitionLockRepository> {

  /**
   * Deletes Acquisition Lock by participant and training instance
   *
   * @param participantRefId - id of participant associated with lock
   * @param trainingInstanceId - id of training instance associated with lock
   */
  @Modifying
  void deleteByParticipantRefIdAndTrainingInstanceId(
      @Param("participantRefId") Long participantRefId,
      @Param("trainingInstanceId") Long trainingInstanceId);
}
