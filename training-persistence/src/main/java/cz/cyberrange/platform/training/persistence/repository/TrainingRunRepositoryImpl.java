package cz.cyberrange.platform.training.persistence.repository;

import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import cz.cyberrange.platform.training.persistence.model.QTrainingDefinition;
import cz.cyberrange.platform.training.persistence.model.QTrainingInstance;
import cz.cyberrange.platform.training.persistence.model.QTrainingRun;
import cz.cyberrange.platform.training.persistence.model.QUserRef;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public class TrainingRunRepositoryImpl extends QuerydslRepositorySupport
    implements TrainingRunRepositoryCustom {

  @PersistenceContext private EntityManager entityManager;

  /** Instantiates a new Training run repository. */
  public TrainingRunRepositoryImpl() {
    super(TrainingRun.class);
  }

  @Override
  @Transactional
  public Page<TrainingRun> findAllByParticipantRefId(
      @Param("userRefId") Long userRefId, Predicate predicate, Pageable pageable) {

    QTrainingRun trainingRun = QTrainingRun.trainingRun;
    QUserRef participantRef = new QUserRef("participantRef");
    QTrainingInstance trainingInstance = new QTrainingInstance("trainingInstance");
    QTrainingDefinition trainingDefinition = new QTrainingDefinition("trainingDefinition");

    JPQLQuery<TrainingRun> query =
        new JPAQueryFactory(entityManager)
            .selectFrom(trainingRun)
            .distinct()
            .leftJoin(trainingRun.participantRef, participantRef)
            .leftJoin(trainingRun.trainingInstance, trainingInstance)
            .leftJoin(trainingInstance.trainingDefinition, trainingDefinition)
            .where(participantRef.userRefId.eq(userRefId));

    if (predicate != null) {
      query.where(predicate);
    }
    return getPage(query, pageable);
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
