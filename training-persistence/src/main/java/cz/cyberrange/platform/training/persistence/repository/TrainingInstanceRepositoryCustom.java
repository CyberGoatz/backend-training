package cz.cyberrange.platform.training.persistence.repository;

import com.querydsl.core.types.Predicate;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** The interface Training instance repository custom. */
public interface TrainingInstanceRepositoryCustom {

  /**
   * Find all training instances of logged in user.
   *
   * @param predicate the predicate
   * @param pageable the pageable
   * @param loggedInUserId the logged in user id
   * @return the page of training instances
   */
  Page<TrainingInstance> findAll(Predicate predicate, Pageable pageable, Long loggedInUserId);
}
