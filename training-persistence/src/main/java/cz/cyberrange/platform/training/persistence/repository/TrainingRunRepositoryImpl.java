package cz.cyberrange.platform.training.persistence.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import cz.cyberrange.platform.training.api.enums.Actions;
import cz.cyberrange.platform.training.persistence.model.QTrainingDefinition;
import cz.cyberrange.platform.training.persistence.model.QTrainingInstance;
import cz.cyberrange.platform.training.persistence.model.QTrainingRun;
import cz.cyberrange.platform.training.persistence.model.QUserRef;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import cz.cyberrange.platform.training.persistence.model.enums.TRState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collection;

public class TrainingRunRepositoryImpl extends QuerydslRepositorySupport implements TrainingRunRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Instantiates a new Training run repository.
     */
    public TrainingRunRepositoryImpl() {
        super(TrainingRun.class);
    }

    @Override
    @Transactional
    public Page<TrainingRun> findAllByParticipantRefId(@Param("userRefId") Long userRefId, Predicate predicate, Pageable pageable) {

        QTrainingRun trainingRun = QTrainingRun.trainingRun;
        QUserRef participantRef = new QUserRef("participantRef");
        QTrainingInstance trainingInstance = new QTrainingInstance("trainingInstance");
        QTrainingDefinition trainingDefinition = new QTrainingDefinition("trainingDefinition");

        JPQLQuery<TrainingRun> query = new JPAQueryFactory(entityManager).selectFrom(trainingRun).distinct()
                .leftJoin(trainingRun.participantRef, participantRef)
                .leftJoin(trainingRun.trainingInstance, trainingInstance)
                .leftJoin(trainingInstance.trainingDefinition, trainingDefinition)
                .where(participantRef.userRefId.eq(userRefId));

        if (predicate != null) {
            query.where(predicate);
        }
        return getPage(query, pageable);
    }

    @Override
    @Transactional
    public Page<TrainingRun> findAllByParticipantRefIdAndPossibleActions(@Param("userRefId") Long userRefId,
                                                                         Predicate predicate,
                                                                         Collection<Actions> actions,
                                                                         Pageable pageable) {
        QTrainingRun trainingRun = QTrainingRun.trainingRun;
        QUserRef participantRef = new QUserRef("participantRef");
        QTrainingInstance trainingInstance = new QTrainingInstance("trainingInstance");
        QTrainingDefinition trainingDefinition = new QTrainingDefinition("trainingDefinition");

        JPQLQuery<TrainingRun> query = new JPAQueryFactory(entityManager).selectFrom(trainingRun).distinct()
                .leftJoin(trainingRun.participantRef, participantRef)
                .leftJoin(trainingRun.trainingInstance, trainingInstance)
                .leftJoin(trainingInstance.trainingDefinition, trainingDefinition)
                .where(participantRef.userRefId.eq(userRefId));

        if (predicate != null) {
            query.where(predicate);
        }

        BooleanBuilder actionPredicate = buildPossibleActionPredicate(
                trainingRun,
                trainingInstance,
                userRefId,
                actions
        );
        if (actionPredicate.hasValue()) {
            query.where(actionPredicate);
        }

        return getPage(query, pageable);
    }

    private BooleanBuilder buildPossibleActionPredicate(QTrainingRun trainingRun,
                                                        QTrainingInstance trainingInstance,
                                                        Long userRefId,
                                                        Collection<Actions> actions) {
        BooleanBuilder actionPredicate = new BooleanBuilder();
        if (actions == null || actions.isEmpty()) {
            return actionPredicate;
        }

        QTrainingRun finishedRun = new QTrainingRun("finishedRun");
        Predicate noFinishedRunForInstance = JPAExpressions.selectOne()
                .from(finishedRun)
                .where(finishedRun.participantRef.userRefId.eq(userRefId)
                        .and(finishedRun.trainingInstance.id.eq(trainingInstance.id))
                        .and(finishedRun.state.eq(TRState.FINISHED)))
                .notExists();
        LocalDateTime now = LocalDateTime.now(Clock.systemUTC());
        actions.forEach(action -> {
            if (action == Actions.SANDBOX_EXPIRED) {
                actionPredicate.or(trainingRun.state.eq(TRState.EXPIRED)
                        .and(noFinishedRunForInstance));
            } else if (action == Actions.RESULTS) {
                actionPredicate.or(trainingRun.state.ne(TRState.EXPIRED)
                        .and(trainingRun.state.eq(TRState.FINISHED)
                                .or(trainingInstance.endTime.isNotNull().and(trainingInstance.endTime.before(now)))));
            } else if (action == Actions.RESUME) {
                actionPredicate.or(trainingRun.state.ne(TRState.EXPIRED)
                        .and(trainingRun.state.ne(TRState.FINISHED))
                        .and(trainingInstance.endTime.isNull().or(trainingInstance.endTime.after(now)))
                        .and(noFinishedRunForInstance));
            }
        });
        return actionPredicate;
    }

    private <T> Page getPage(JPQLQuery<T> query, Pageable pageable) {
        if (pageable == null) {
            pageable = PageRequest.of(0, 20);
        }
        query = getQuerydsl().applyPagination(pageable, query);
        long count = query.fetchCount();
        return new PageImpl<>(query.fetch(), pageable, count);
    }
}
