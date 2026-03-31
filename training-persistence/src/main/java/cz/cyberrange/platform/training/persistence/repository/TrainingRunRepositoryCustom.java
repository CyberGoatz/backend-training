package cz.cyberrange.platform.training.persistence.repository;

import com.querydsl.core.types.Predicate;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

/** The interface Training instance repository custom. */
public interface TrainingRunRepositoryCustom {

  /**
   * Find all training instances of logged in user.
   *
   * @param userRefId the participant ref id
   * @param predicate represents a predicate (boolean-valued function) of one argument.
   * @param pageable the pageable
   * @return the page of training instances
   */
  Page<TrainingRun> findAllByParticipantRefId(
      @Param("userRefId") Long userRefId, Predicate predicate, Pageable pageable);
}
