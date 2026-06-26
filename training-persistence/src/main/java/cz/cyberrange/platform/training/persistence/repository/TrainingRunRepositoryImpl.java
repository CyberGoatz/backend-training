package cz.cyberrange.platform.training.persistence.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
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
        return findAllByParticipantRefId(userRefId, predicate, pageable, null);
    }

    @Override
    @Transactional
    public Page<TrainingRun> findAllByParticipantRefId(@Param("userRefId") Long userRefId,
                                                       Predicate predicate,
                                                       Pageable pageable,
                                                       String sortByTitle) {

        QTrainingRun trainingRun = QTrainingRun.trainingRun;
        QUserRef participantRef = new QUserRef("participantRef");
        QTrainingInstance trainingInstance = new QTrainingInstance("trainingInstance");
        QTrainingDefinition trainingDefinition = new QTrainingDefinition("trainingDefinition");

        JPQLQuery<TrainingRun> countQuery = buildAccessedTrainingRunsQuery(
                userRefId, predicate, trainingRun, participantRef, trainingInstance, trainingDefinition, false);
        JPQLQuery<TrainingRun> contentQuery = buildAccessedTrainingRunsQuery(
                userRefId, predicate, trainingRun, participantRef, trainingInstance, trainingDefinition, true);
        applyTitleSort(contentQuery, trainingInstance, trainingRun, sortByTitle);

        return getPage(contentQuery, countQuery, pageable);
    }

    @Override
    @Transactional
    public Page<TrainingRun> findAllByParticipantRefIdAndPossibleActions(@Param("userRefId") Long userRefId,
                                                                         Predicate predicate,
                                                                         Collection<Actions> actions,
                                                                         Pageable pageable) {
        return findAllByParticipantRefIdAndPossibleActions(userRefId, predicate, actions, pageable, null);
    }

    @Override
    @Transactional
    public Page<TrainingRun> findAllByParticipantRefIdAndPossibleActions(@Param("userRefId") Long userRefId,
                                                                         Predicate predicate,
                                                                         Collection<Actions> actions,
                                                                         Pageable pageable,
                                                                         String sortByTitle) {
        QTrainingRun trainingRun = QTrainingRun.trainingRun;
        QUserRef participantRef = new QUserRef("participantRef");
        QTrainingInstance trainingInstance = new QTrainingInstance("trainingInstance");
        QTrainingDefinition trainingDefinition = new QTrainingDefinition("trainingDefinition");

        JPQLQuery<TrainingRun> countQuery = buildAccessedTrainingRunsQuery(
                userRefId, predicate, trainingRun, participantRef, trainingInstance, trainingDefinition, false);
        JPQLQuery<TrainingRun> contentQuery = buildAccessedTrainingRunsQuery(
                userRefId, predicate, trainingRun, participantRef, trainingInstance, trainingDefinition, true);

        BooleanBuilder actionPredicate = buildPossibleActionPredicate(
                trainingRun,
                trainingInstance,
                userRefId,
                actions
        );
        if (actionPredicate.hasValue()) {
            countQuery.where(actionPredicate);
            contentQuery.where(actionPredicate);
        }
        applyTitleSort(contentQuery, trainingInstance, trainingRun, sortByTitle);

        return getPage(contentQuery, countQuery, pageable);
    }

    private JPQLQuery<TrainingRun> buildAccessedTrainingRunsQuery(Long userRefId,
                                                                  Predicate predicate,
                                                                  QTrainingRun trainingRun,
                                                                  QUserRef participantRef,
                                                                  QTrainingInstance trainingInstance,
                                                                  QTrainingDefinition trainingDefinition,
                                                                  boolean fetchJoin) {
        JPQLQuery<TrainingRun> query = new JPAQueryFactory(entityManager).selectFrom(trainingRun).distinct();
        if (fetchJoin) {
            query.leftJoin(trainingRun.participantRef, participantRef).fetchJoin()
                    .leftJoin(trainingRun.trainingInstance, trainingInstance).fetchJoin()
                    .leftJoin(trainingInstance.trainingDefinition, trainingDefinition).fetchJoin()
                    .leftJoin(trainingRun.currentLevel).fetchJoin();
        } else {
            query.leftJoin(trainingRun.participantRef, participantRef)
                    .leftJoin(trainingRun.trainingInstance, trainingInstance)
                    .leftJoin(trainingInstance.trainingDefinition, trainingDefinition);
        }
        query.where(participantRef.userRefId.eq(userRefId));
        if (predicate != null) {
            query.where(predicate);
        }
        return query;
    }

    private void applyTitleSort(JPQLQuery<TrainingRun> query,
                                QTrainingInstance trainingInstance,
                                QTrainingRun trainingRun,
                                String sortByTitle) {
        if ("asc".equals(sortByTitle)) {
            query.orderBy(trainingInstance.title.asc(), trainingRun.id.asc());
        } else if ("desc".equals(sortByTitle)) {
            query.orderBy(trainingInstance.title.desc(), trainingRun.id.desc());
        }
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

        if (!actions.contains(Actions.RESULTS)
                && (actions.contains(Actions.RESUME) || actions.contains(Actions.SANDBOX_EXPIRED))) {
            actionPredicate.and(buildPreferredContinueRunPredicate(trainingRun, trainingInstance, userRefId, now));
        }
        return actionPredicate;
    }

    private Predicate buildPreferredContinueRunPredicate(QTrainingRun trainingRun,
                                                         QTrainingInstance trainingInstance,
                                                         Long userRefId,
                                                         LocalDateTime now) {
        QTrainingRun preferredRun = new QTrainingRun("preferredRun");

        BooleanExpression currentRunIsResumeCandidate = trainingRun.state.ne(TRState.EXPIRED)
                .and(trainingRun.state.ne(TRState.FINISHED))
                .and(trainingInstance.endTime.isNull().or(trainingInstance.endTime.after(now)));
        BooleanExpression preferredRunIsResumeCandidate = preferredRun.state.ne(TRState.EXPIRED)
                .and(preferredRun.state.ne(TRState.FINISHED))
                .and(preferredRun.trainingInstance.endTime.isNull().or(preferredRun.trainingInstance.endTime.after(now)));
        BooleanExpression preferredResumeRunExists = preferredRunIsResumeCandidate
                .and(trainingRun.state.eq(TRState.EXPIRED)
                        .or(currentRunIsResumeCandidate.and(preferredRun.id.gt(trainingRun.id))));
        BooleanExpression newerExpiredRunExists = preferredRun.state.eq(TRState.EXPIRED)
                .and(trainingRun.state.eq(TRState.EXPIRED))
                .and(preferredRun.id.gt(trainingRun.id));

        return JPAExpressions.selectOne()
                .from(preferredRun)
                .where(preferredRun.participantRef.userRefId.eq(userRefId)
                        .and(preferredRun.trainingInstance.id.eq(trainingInstance.id))
                        .and(preferredRun.id.ne(trainingRun.id))
                        .and(preferredResumeRunExists.or(newerExpiredRunExists)))
                .notExists();
    }

    private <T> Page getPage(JPQLQuery<T> contentQuery, JPQLQuery<T> countQuery, Pageable pageable) {
        if (pageable == null) {
            pageable = PageRequest.of(0, 20);
        }
        long count = countQuery.fetchCount();
        contentQuery = getQuerydsl().applyPagination(pageable, contentQuery);
        return new PageImpl<>(contentQuery.fetch(), pageable, count);
    }
}
