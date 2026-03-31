package cz.cyberrange.platform.training.persistence.repository;

import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import cz.cyberrange.platform.training.persistence.model.QBetaTestingGroup;
import cz.cyberrange.platform.training.persistence.model.QTrainingDefinition;
import cz.cyberrange.platform.training.persistence.model.QUserRef;
import cz.cyberrange.platform.training.persistence.model.TrainingDefinition;
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

/** The type Training definition repository. */
@Repository
public class TrainingDefinitionRepositoryImpl extends QuerydslRepositorySupport
    implements TrainingDefinitionRepositoryCustom {

  @PersistenceContext private EntityManager entityManager;

  /** Instantiates a new Training definition repository. */
  public TrainingDefinitionRepositoryImpl() {
    super(TrainingDefinition.class);
  }

  @Override
  @Transactional
  public Page<TrainingDefinition> findAll(
      Predicate predicate, Pageable pageable, Long loggedInUserId) {
    Objects.requireNonNull(loggedInUserId, "Input logged in user ID must not be null.");
    QTrainingDefinition trainingDefinition = QTrainingDefinition.trainingDefinition;
    QUserRef authors = new QUserRef("authors");
    QUserRef organizers = new QUserRef("organizers");
    QBetaTestingGroup betaTestingGroup = QBetaTestingGroup.betaTestingGroup;

    JPQLQuery<TrainingDefinition> query =
        new JPAQueryFactory(entityManager)
            .selectFrom(trainingDefinition)
            .distinct()
            .leftJoin(trainingDefinition.authors, authors)
            .leftJoin(trainingDefinition.betaTestingGroup, betaTestingGroup)
            .leftJoin(trainingDefinition.betaTestingGroup.organizers, organizers)
            .where(
                authors.userRefId.eq(loggedInUserId).or(organizers.userRefId.eq(loggedInUserId)));

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
