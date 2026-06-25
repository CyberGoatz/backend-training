package cz.cyberrange.platform.training.persistence.repository;

import com.querydsl.core.types.Predicate;
import cz.cyberrange.platform.training.api.enums.Actions;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.util.Collection;

/**
 * The interface Training instance repository custom.
 */
public interface TrainingRunRepositoryCustom {

    /**
     * Find all training instances of logged in user.
     *
     * @param userRefId the participant ref id
     * @param predicate represents a predicate (boolean-valued function) of one argument.
     * @param pageable  the pageable
     * @return the page of training instances
     */
    Page<TrainingRun> findAllByParticipantRefId(@Param("userRefId") Long userRefId, Predicate predicate, Pageable pageable);

    /**
     * Find all training instances of logged in user.
     *
     * @param userRefId   the participant ref id
     * @param predicate   represents a predicate (boolean-valued function) of one argument.
     * @param pageable    the pageable
     * @param sortByTitle optional title sort direction
     * @return the page of training instances
     */
    Page<TrainingRun> findAllByParticipantRefId(@Param("userRefId") Long userRefId,
                                                Predicate predicate,
                                                Pageable pageable,
                                                String sortByTitle);

    /**
     * Find accessed training runs of logged in user by possible learner action.
     *
     * @param userRefId the participant ref id
     * @param predicate represents a predicate (boolean-valued function) of one argument.
     * @param actions   possible learner actions to include
     * @param pageable  the pageable
     * @return the page of training runs
     */
    Page<TrainingRun> findAllByParticipantRefIdAndPossibleActions(@Param("userRefId") Long userRefId,
                                                                   Predicate predicate,
                                                                   Collection<Actions> actions,
                                                                   Pageable pageable);

    /**
     * Find accessed training runs of logged in user by possible learner action.
     *
     * @param userRefId   the participant ref id
     * @param predicate   represents a predicate (boolean-valued function) of one argument.
     * @param actions     possible learner actions to include
     * @param pageable    the pageable
     * @param sortByTitle optional title sort direction
     * @return the page of training runs
     */
    Page<TrainingRun> findAllByParticipantRefIdAndPossibleActions(@Param("userRefId") Long userRefId,
                                                                   Predicate predicate,
                                                                   Collection<Actions> actions,
                                                                   Pageable pageable,
                                                                   String sortByTitle);
}
