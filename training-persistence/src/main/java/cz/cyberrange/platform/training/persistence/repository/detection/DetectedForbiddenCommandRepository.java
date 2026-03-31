package cz.cyberrange.platform.training.persistence.repository.detection;

import cz.cyberrange.platform.training.persistence.model.detection.DetectedForbiddenCommand;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

public interface DetectedForbiddenCommandRepository
    extends JpaRepository<DetectedForbiddenCommand, Long>,
        QuerydslPredicateExecutor<DetectedForbiddenCommand> {

  /**
   * Finds all detected forbidden commands by detection event id.
   *
   * @param eventId the detection event id
   * @param pageable the pageable
   */
  Page<DetectedForbiddenCommand> findAllByEventId(
      @Param("eventId") Long eventId, @Param("pageable") Pageable pageable);

  /**
   * Finds all detected forbidden commands by detection event id.
   *
   * @param eventId the detection event id
   */
  List<DetectedForbiddenCommand> findAllByEventId(@Param("eventId") Long eventId);

  /**
   * Delete all detected forbidden commands of detection event.
   *
   * @param eventId the event id
   */
  @Modifying
  void deleteAllByDetectionEventId(@Param("eventId") Long eventId);
}
