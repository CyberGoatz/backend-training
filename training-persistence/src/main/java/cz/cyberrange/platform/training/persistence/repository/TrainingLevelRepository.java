package cz.cyberrange.platform.training.persistence.repository;

import cz.cyberrange.platform.training.persistence.model.TrainingLevel;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** The JPA repository interface to manage {@link TrainingLevel} instances. */
@Repository
public interface TrainingLevelRepository
    extends JpaRepository<TrainingLevel, Long>, QuerydslPredicateExecutor<TrainingLevel> {
  /**
   * Check if the reference solution is defined for the given training definition.
   *
   * @param trainingDefinitionId the training definition id
   * @return true if at least one of the training levels has reference solution defined, false
   *     otherwise.
   */
  @Query(
      "SELECT CASE WHEN COUNT(tl) > 0 THEN true ELSE false END "
          + "FROM TrainingLevel tl "
          + "INNER JOIN tl.trainingDefinition td "
          + "WHERE td.id = :trainingDefinitionId AND tl.referenceSolution NOT LIKE '[]'")
  boolean hasReferenceSolution(@Param("trainingDefinitionId") Long trainingDefinitionId);

  List<TrainingLevel> findAllByTrainingDefinitionId(
      @Param("trainingDefinitionId") Long trainingDefinitionId);
}
