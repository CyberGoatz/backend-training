package cz.cyberrange.platform.training.persistence.repository.detection;

import cz.cyberrange.platform.training.persistence.model.detection.NoCommandsDetectionEvent;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

public interface NoCommandsDetectionEventRepository
    extends JpaRepository<NoCommandsDetectionEvent, Long>,
        QuerydslPredicateExecutor<NoCommandsDetectionEvent> {

  /**
   * Returns the detection event based on its id
   *
   * @param eventId the detection event id
   */
  NoCommandsDetectionEvent findNoCommandsEventById(@Param("eventId") Long eventId);

  /**
   * Returns all no commands detection events of cheating detection
   *
   * @param cheatingDetectionId the detection event id
   */
  List<NoCommandsDetectionEvent> findAllByCheatingDetectionId(
      @Param("cheatingDetectionId") Long cheatingDetectionId);
}
