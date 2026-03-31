package cz.cyberrange.platform.training.persistence.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.StringPath;
import cz.cyberrange.platform.training.persistence.model.QTrainingInstance;
import cz.cyberrange.platform.training.persistence.model.TrainingDefinition;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** The JPA repository interface to manage {@link TrainingInstance} instances. */
@Repository
public interface TrainingInstanceRepository
    extends JpaRepository<TrainingInstance, Long>,
        TrainingInstanceRepositoryCustom,
        QuerydslPredicateExecutor<TrainingInstance>,
        QuerydslBinderCustomizer<QTrainingInstance> {

  /**
   * That method is used to make the query dsl string values case insensitive and also it supports
   * partial matches in the database.
   *
   * @param querydslBindings
   * @param qTrainingInstance
   */
  @Override
  default void customize(QuerydslBindings querydslBindings, QTrainingInstance qTrainingInstance) {
    querydslBindings
        .bind(String.class)
        .all(
            (StringPath path, Collection<? extends String> values) -> {
              BooleanBuilder predicate = new BooleanBuilder();
              values.forEach(value -> predicate.and(path.containsIgnoreCase(value)));
              return Optional.ofNullable(predicate);
            });
  }

  /**
   * Find all training instances by id of associated training definition.
   *
   * @param trainingDefId the training def id
   * @return the list of {@link TrainingInstance}s associated to {@link TrainingDefinition}
   */
  List<TrainingInstance> findAllByTrainingDefinitionId(@Param("trainingDefId") Long trainingDefId);

  /**
   * Find all training instances
   *
   * @param predicate the predicate
   * @param pageable the pageable
   * @return page of all {@link TrainingInstance}
   */
  @EntityGraph(
      value = "TrainingInstance.findAllAuthorsOrganizersBetaTestingGroupBetaTestingGroupOrganizers",
      type = EntityGraph.EntityGraphType.FETCH)
  Page<TrainingInstance> findAll(Predicate predicate, Pageable pageable);

  /**
   * Find training instance by id
   *
   * @param id id of training instance
   * @return {@link TrainingInstance}
   */
  @EntityGraph(
      value = "TrainingInstance.findByIdAuthorsOrganizers",
      type = EntityGraph.EntityGraphType.FETCH)
  Optional<TrainingInstance> findById(Long id);

  /**
   * Find training instance with start time in the past, end time in the future and by corresponding
   * access token.
   *
   * @param datetime the current time
   * @param accessToken the access token
   * @return {@link TrainingInstance} with start time in the past, end time in the future and by
   *     corresponding access token
   */
  Optional<TrainingInstance> findByStartTimeAfterAndEndTimeBeforeAndAccessToken(
      @Param("datetime") LocalDateTime datetime, @Param("accessToken") String accessToken);

  /**
   * Check if any training instances are associated with training definition
   *
   * @param trainingDefinitionId the training definition id
   * @return True if there are any instances associated with training definition
   */
  boolean existsAnyForTrainingDefinition(@Param("trainingDefinitionId") Long trainingDefinitionId);

  /**
   * Check if training instance exists for given access token.
   *
   * @param accessToken the access token
   * @return True if there is any instance with given access token
   */
  @Query(
      "SELECT COUNT(*) > 0 FROM TrainingInstance instance WHERE instance.accessToken = :accessToken")
  boolean existsForToken(@Param("accessToken") String accessToken);

  /**
   * Find training instance by id including its associated training definition.
   *
   * @param instanceId the instance id
   * @return {@link TrainingInstance} including its associated {@link TrainingDefinition}
   */
  Optional<TrainingInstance> findByIdIncludingDefinition(@Param("instanceId") Long instanceId);

  /**
   * Find training instance by pool id.
   *
   * @param poolId the pool id
   * @return {@link TrainingInstance} including its associated {@link TrainingDefinition}
   */
  Optional<TrainingInstance> findByPoolId(@Param("poolId") Long poolId);

  /**
   * Checks if training instance finished.
   *
   * @param instanceId the instance id
   * @param currentTime the current time
   * @return true if instance is finished, false if not
   */
  boolean isFinished(
      @Param("instanceId") Long instanceId, @Param("currentTime") LocalDateTime currentTime);
}
