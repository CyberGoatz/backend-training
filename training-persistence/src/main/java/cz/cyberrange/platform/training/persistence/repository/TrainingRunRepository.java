package cz.cyberrange.platform.training.persistence.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.StringPath;
import cz.cyberrange.platform.training.persistence.model.QTrainingRun;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * The JPA repository interface to manage {@link TrainingRun} instances.
 */
@Repository
public interface TrainingRunRepository extends JpaRepository<TrainingRun, Long>, TrainingRunRepositoryCustom, QuerydslPredicateExecutor<TrainingRun>, QuerydslBinderCustomizer<QTrainingRun> {

    /**
     * That method is used to make the query dsl string values case insensitive and also it supports partial matches in the database.
     *
     * @param querydslBindings
     * @param qTrainingRun
     */
    @Override
    default void customize(QuerydslBindings querydslBindings, QTrainingRun qTrainingRun) {
        querydslBindings.bind(String.class).all((StringPath path, Collection<? extends String> values) -> {
            BooleanBuilder predicate = new BooleanBuilder();
            values.forEach(value -> predicate.and(path.containsIgnoreCase(value)));
            return Optional.ofNullable(predicate);
        });
    }

    /**
     * Find all training runs
     *
     * @param predicate the predicate
     * @param pageable  the pageable
     * @return page of all {@link TrainingRun}
     */
    @EntityGraph(
            value = "TrainingRun.findAllParticipantRef",
            type = EntityGraph.EntityGraphType.FETCH
    )
    Page<TrainingRun> findAll(Predicate predicate, Pageable pageable);

    /**
     * Find all training runs associated with training instance.
     *
     * @param trainingInstanceId the training instance id
     * @param pageable           the pageable
     * @return the page of all {@link TrainingRun}s associated with {@link TrainingInstance}
     */
    @Query("SELECT tr FROM TrainingRun tr WHERE tr.trainingInstance.id = :trainingInstanceId ORDER BY tr.startTime ASC")
    @EntityGraph(
            value = "TrainingRun.findAllParticipantRef",
            type = EntityGraph.EntityGraphType.FETCH
    )
    Page<TrainingRun> findAllByTrainingInstanceId(@Param("trainingInstanceId") Long trainingInstanceId, Pageable pageable);


    /**
     * Find all training runs associated with training instance.
     *
     * @param trainingInstanceId the training instance id
     * @return the set of all {@link TrainingRun}s associated with {@link TrainingInstance}
     */
    @EntityGraph(
            value = "TrainingRun.findAllParticipantRef",
            type = EntityGraph.EntityGraphType.FETCH
    )
    Set<TrainingRun> findAllByTrainingInstanceId(Long trainingInstanceId);

    /**
     * Find training run by id
     *
     * @param id id of training run
     * @return {@link TrainingRun}
     */
    @EntityGraph(
            value = "TrainingRun.findByIdParticipantRefTrainingInstance",
            type = EntityGraph.EntityGraphType.FETCH
    )
    Optional<TrainingRun> findById(Long id);

    /**
     * Find all training runs accessed by participant by their user ref id.
     *
     * @param userRefId the participant ref id
     * @param pageable  the pageable
     * @return the page of all {@link TrainingRun}s accessed by participant
     */
    Page<TrainingRun> findAllByParticipantRefId(@Param("userRefId") Long userRefId, Pageable pageable);

    /**
     * Find public-safe finished training runs accessed by participant by their user ref id.
     *
     * @param userRefId the participant ref id
     * @return the finished {@link TrainingRun}s accessed by participant
     */
    @Query("SELECT tr FROM TrainingRun tr " +
            "JOIN FETCH tr.participantRef pr " +
            "JOIN FETCH tr.trainingInstance ti " +
            "JOIN FETCH ti.trainingDefinition td " +
            "WHERE pr.userRefId = :userRefId AND tr.state = 'FINISHED' " +
            "ORDER BY tr.endTime DESC")
    List<TrainingRun> findAllFinishedByParticipantRefId(@Param("userRefId") Long userRefId);

    /**
     * Find training run by id including current level
     *
     * @param trainingRunId the training run id
     * @return {@link TrainingRun} including {@link cz.cyberrange.platform.training.persistence.model.AbstractLevel}
     */
    Optional<TrainingRun> findByIdWithLevel(@Param("trainingRunId") Long trainingRunId);

    /**
     * Find all training runs by id of associated training definition that are accessible to participant by user ref id.
     *
     * @param trainingDefinitionId the training definition id
     * @param userRefId            the participant user ref id
     * @param pageable             the pageable
     * @return the page of all {@link TrainingRun}s by id of associated {@link cz.cyberrange.platform.training.persistence.model.TrainingDefinition} that are accessible to participant
     */
    Page<TrainingRun> findAllByTrainingDefinitionIdAndParticipantUserRefId(@Param("trainingDefinitionId") Long trainingDefinitionId,
                                                                           @Param("userRefId") Long userRefId,
                                                                           Pageable pageable);

    /**
     * Find all active training runs by training instance id.
     *
     * @param trainingInstanceId the training instance id
     * @param pageable           the pageable
     * @return the page of all active {@link TrainingRun}s associated with given {@link TrainingInstance}
     */
    Page<TrainingRun> findAllActiveByTrainingInstanceId(@Param("trainingInstanceId") Long trainingInstanceId,
                                                        Pageable pageable);

    /**
     * Find all inactive training runs by training instance id.
     *
     * @param trainingInstanceId the training instance id
     * @param pageable           the pageable
     * @return the page of all inactive {@link TrainingRun}s associated with given {@link TrainingInstance}
     */
    Page<TrainingRun> findAllInactiveByTrainingInstanceId(@Param("trainingInstanceId") Long trainingInstanceId,
                                                          Pageable pageable);

    /**
     * Find all finished training runs by training instance id.
     *
     * @param trainingInstanceId the training instance id
     * @param pageable           the pageable
     * @return the page of all finished {@link TrainingRun}s associated with given {@link TrainingInstance}
     */
    Page<TrainingRun> findAllFinishedByTrainingInstanceId(@Param("trainingInstanceId") Long trainingInstanceId,
                                                          Pageable pageable);

    /**
     * Find all training runs associated with training definition.
     *
     * @param trainingDefinitionId the training definition id
     * @param pageable             the pageable
     * @return the page of all {@link TrainingRun}s associated with {@link cz.cyberrange.platform.training.persistence.model.TrainingDefinition}
     */
    Page<TrainingRun> findAllByTrainingDefinitionId(@Param("trainingDefinitionId") Long trainingDefinitionId,
                                                    Pageable pageable);

    /**
     * Delete all training runs by training instance.
     *
     * @param trainingInstanceId the training instance id
     */
    @Modifying
    void deleteTrainingRunsByTrainingInstance(@Param("trainingInstanceId") Long trainingInstanceId);

    /**
     * Find valid training run by user and access token.
     *
     * @param accessToken the access token
     * @param userRefId   the user ref id
     * @return the {@link TrainingRun} by user and access token
     */
    Optional<TrainingRun> findRunningTrainingRunOfUser(@Param("accessToken") String accessToken, @Param("userRefId") Long userRefId);

    /**
     * Checks whether the participant has already finished a training run in the training instance.
     *
     * @param accessToken the access token of the training instance
     * @param userRefId   the user ref id
     * @return true if the participant has a finished training run in the training instance
     */
    @Query("SELECT (COUNT(tr) > 0) FROM TrainingRun tr " +
            "JOIN tr.trainingInstance ti " +
            "JOIN tr.participantRef pr " +
            "WHERE ti.accessToken = :accessToken AND pr.userRefId = :userRefId AND tr.state = 'FINISHED'")
    boolean existsFinishedTrainingRunOfUser(@Param("accessToken") String accessToken, @Param("userRefId") Long userRefId);

    /**
     * Exists any for training instance boolean.
     *
     * @param trainingInstanceId the training instance id
     * @return the boolean
     */
    boolean existsAnyForTrainingInstance(@Param("trainingInstanceId") Long trainingInstanceId);

}
