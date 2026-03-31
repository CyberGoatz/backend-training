package cz.cyberrange.platform.training.persistence.repository;

import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import cz.cyberrange.platform.training.persistence.model.QTrainingInstance;
import cz.cyberrange.platform.training.persistence.model.QUserRef;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import java.util.Objects;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** The type Training instance repository. */
@Repository
public class TrainingInstanceRepositoryImpl extends QuerydslRepositorySupport
    implements TrainingInstanceRepositoryCustom {

  @PersistenceContext private EntityManager entityManager;

  /** Instantiates a new Training instance repository. */
  public TrainingInstanceRepositoryImpl() {
    super(TrainingInstance.class);
  }

  @Override
  @Transactional
  public Page<TrainingInstance> findAll(
      Predicate predicate, Pageable pageable, Long loggedInUserId) {
    Objects.requireNonNull(loggedInUserId, "Input logged in user ID must not be null.");
    QTrainingInstance trainingInstance = QTrainingInstance.trainingInstance;
    QUserRef organizers = new QUserRef("organizers");

    JPQLQuery<TrainingInstance> query =
        new JPAQueryFactory(entityManager)
            .selectFrom(trainingInstance)
            .distinct()
            .leftJoin(trainingInstance.organizers, organizers)
            .where(organizers.userRefId.eq(loggedInUserId));

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
