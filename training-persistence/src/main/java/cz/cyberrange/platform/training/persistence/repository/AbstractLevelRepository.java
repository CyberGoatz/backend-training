package cz.cyberrange.platform.training.persistence.repository;

import cz.cyberrange.platform.training.persistence.model.AbstractLevel;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** The JPA repository interface to manage {@link AbstractLevel} instances. */
@Repository
public interface AbstractLevelRepository
    extends JpaRepository<AbstractLevel, Long>, QuerydslPredicateExecutor<AbstractLevel> {

  /**
   * Find all levels by training definition id.
   *
   * @param trainingDefinitionId the training definition id
   * @return list of {@link AbstractLevel}s associated with {@link
   *     cz.cyberrange.platform.training.persistence.model.TrainingDefinition}
   */
  List<AbstractLevel> findAllLevelsByTrainingDefinitionId(
      @Param("trainingDefinitionId") Long trainingDefinitionId);

  /**
   * Find all levels by level ids.
   *
   * @param levelIds the ids of the levels
   * @param trainingDefinitionId the training definition id
   * @return list of {@link AbstractLevel}s with the given ids
   */
  List<AbstractLevel> findAllByIdIsInAndTrainingDefinitionId(
      List<Long> levelIds, Long trainingDefinitionId);

  /**
   * Find first level for particular training definition
   *
   * @param trainingDefinitionId the training definition id
   * @return {@link AbstractLevel}s associated with {@link
   *     cz.cyberrange.platform.training.persistence.model.TrainingDefinition}
   */
  List<AbstractLevel> findFirstLevelByTrainingDefinitionId(
      @Param("trainingDefinitionId") Long trainingDefinitionId, Pageable pageable);

  /**
   * Find level in definition.
   *
   * @param trainingDefinitionId the training definition id
   * @param levelId the level id
   * @return {@link AbstractLevel} from {@link
   *     cz.cyberrange.platform.training.persistence.model.TrainingDefinition} by ids
   */
  Optional<AbstractLevel> findLevelInDefinition(
      @Param("trainingDefinitionId") Long trainingDefinitionId, @Param("levelId") Long levelId);

  /**
   * Find by id including definition optional.
   *
   * @param levelId the level id
   * @return {@link AbstractLevel} with its associated {@link
   *     cz.cyberrange.platform.training.persistence.model.TrainingDefinition}
   */
  Optional<AbstractLevel> findByIdIncludingDefinition(@Param("levelId") Long levelId);

  /**
   * Gets current max order.
   *
   * @param trainingDefinitionId the training definition id
   * @return the current max order of {@link AbstractLevel} in given {@link
   *     cz.cyberrange.platform.training.persistence.model.TrainingDefinition}
   */
  Integer getCurrentMaxOrder(@Param("trainingDefinitionId") Long trainingDefinitionId);

  /**
   * Increase level order from given order to the given order.
   *
   * @param fromOrder first level which order will be increased
   * @param toOrder last level which order will be increased.
   */
  @Modifying
  void increaseOrderOfLevels(
      @Param("trainingDefinitionId") Long trainingDefinitionId,
      @Param("fromOrder") Integer fromOrder,
      @Param("toOrder") Integer toOrder);

  /**
   * Decrease level order from given order to the given order.
   *
   * @param fromOrder first level which order will be decreased
   * @param toOrder last level which order will be decreased.
   */
  @Modifying
  void decreaseOrderOfLevels(
      @Param("trainingDefinitionId") Long trainingDefinitionId,
      @Param("fromOrder") Integer fromOrder,
      @Param("toOrder") Integer toOrder);
}
