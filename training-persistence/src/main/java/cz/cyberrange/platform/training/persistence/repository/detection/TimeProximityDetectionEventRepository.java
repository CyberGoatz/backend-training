package cz.cyberrange.platform.training.persistence.repository.detection;

import cz.cyberrange.platform.training.persistence.model.detection.TimeProximityDetectionEvent;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

public interface TimeProximityDetectionEventRepository
    extends JpaRepository<TimeProximityDetectionEvent, Long>,
        QuerydslPredicateExecutor<TimeProximityDetectionEvent> {

  /**
   * Returns the detection event based on its id
   *
   * @param eventId the detection event id
   */
  TimeProximityDetectionEvent findTimeProximityEventById(@Param("eventId") Long eventId);

  /**
   * Returns all time proximity detection events of cheating detection
   *
   * @param cheatingDetectionId the detection event id
   */
  List<TimeProximityDetectionEvent> findAllByCheatingDetectionId(
      @Param("cheatingDetectionId") Long cheatingDetectionId);
}
