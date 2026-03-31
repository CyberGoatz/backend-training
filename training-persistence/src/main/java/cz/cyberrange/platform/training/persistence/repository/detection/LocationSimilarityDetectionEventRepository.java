package cz.cyberrange.platform.training.persistence.repository.detection;

import cz.cyberrange.platform.training.persistence.model.detection.LocationSimilarityDetectionEvent;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

public interface LocationSimilarityDetectionEventRepository
    extends JpaRepository<LocationSimilarityDetectionEvent, Long>,
        QuerydslPredicateExecutor<LocationSimilarityDetectionEvent> {

  /**
   * Returns the detection event based on its id
   *
   * @param eventId the detection event id
   */
  LocationSimilarityDetectionEvent findLocationSimilarityEventById(@Param("eventId") Long eventId);

  /**
   * Returns all location similarity detection events of cheating detection
   *
   * @param cheatingDetectionId the detection event id
   */
  List<LocationSimilarityDetectionEvent> findAllByCheatingDetectionId(
      @Param("cheatingDetectionId") Long cheatingDetectionId);
}
