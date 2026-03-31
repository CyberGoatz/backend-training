package cz.cyberrange.platform.training.persistence.repository;

import cz.cyberrange.platform.training.persistence.model.InfoLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

/** The JPA repository interface to manage {@link InfoLevel} instances. */
@Repository
public interface InfoLevelRepository
    extends JpaRepository<InfoLevel, Long>, QuerydslPredicateExecutor<InfoLevel> {}
