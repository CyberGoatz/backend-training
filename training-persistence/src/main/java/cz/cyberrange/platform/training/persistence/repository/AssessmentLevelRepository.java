package cz.cyberrange.platform.training.persistence.repository;

import cz.cyberrange.platform.training.persistence.model.AssessmentLevel;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** The JPA repository interface to manage {@link AssessmentLevel} instances. */
@Repository
public interface AssessmentLevelRepository
    extends JpaRepository<AssessmentLevel, Long>, QuerydslPredicateExecutor<AssessmentLevel> {

  List<AssessmentLevel> findAllByTrainingDefinitionId(
      @Param("trainingDefinitionId") Long trainingDefinitionId);
}
