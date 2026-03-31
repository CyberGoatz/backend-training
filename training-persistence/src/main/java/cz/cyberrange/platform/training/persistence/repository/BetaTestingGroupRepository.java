package cz.cyberrange.platform.training.persistence.repository;

import cz.cyberrange.platform.training.persistence.model.BetaTestingGroup;
import cz.cyberrange.platform.training.persistence.model.InfoLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

/** The JPA repository interface to manage {@link BetaTestingGroup} instances. */
@Repository
public interface BetaTestingGroupRepository
    extends JpaRepository<BetaTestingGroup, Long>, QuerydslPredicateExecutor<InfoLevel> {}
