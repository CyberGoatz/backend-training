package cz.cyberrange.platform.training.persistence.repository;

import com.querydsl.core.types.Predicate;
import cz.cyberrange.platform.training.persistence.model.TrainingDefinition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** The interface Training definition repository custom. */
public interface TrainingDefinitionRepositoryCustom {

  /**
   * Find all training definitions.
   *
   * @param predicate the predicate
   * @param pageable the pageable
   * @param loggedInUserId the logged in user id
   * @return the page of training definitions
   */
  Page<TrainingDefinition> findAll(Predicate predicate, Pageable pageable, Long loggedInUserId);
}
