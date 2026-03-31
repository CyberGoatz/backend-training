package cz.cyberrange.platform.training.persistence.repository.detection;

import cz.cyberrange.platform.training.persistence.model.detection.MinimalSolveTimeDetectionEvent;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

public interface MinimalSolveTimeDetectionEventRepository
    extends JpaRepository<MinimalSolveTimeDetectionEvent, Long>,
        QuerydslPredicateExecutor<MinimalSolveTimeDetectionEvent> {

  /**
   * Returns the detection event based on its id
   *
   * @param eventId the detection event id
   */
  MinimalSolveTimeDetectionEvent findMinimalSolveTimeEventById(@Param("eventId") Long eventId);

  /**
   * Returns all minimal solve time detection events of cheating detection
   *
   * @param cheatingDetectionId the detection event id
   */
  List<MinimalSolveTimeDetectionEvent> findAllByCheatingDetectionId(
      @Param("cheatingDetectionId") Long cheatingDetectionId);
}
