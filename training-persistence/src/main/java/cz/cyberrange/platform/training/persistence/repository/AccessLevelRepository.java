package cz.cyberrange.platform.training.persistence.repository;

import cz.cyberrange.platform.training.persistence.model.AccessLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

/** The JPA repository interface to manage {@link AccessLevel} instances. */
@Repository
public interface AccessLevelRepository
    extends JpaRepository<AccessLevel, Long>, QuerydslPredicateExecutor<AccessLevel> {}
