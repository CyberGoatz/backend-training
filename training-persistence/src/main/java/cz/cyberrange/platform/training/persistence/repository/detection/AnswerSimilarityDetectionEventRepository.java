package cz.cyberrange.platform.training.persistence.repository.detection;

import cz.cyberrange.platform.training.persistence.model.detection.AnswerSimilarityDetectionEvent;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

public interface AnswerSimilarityDetectionEventRepository
    extends JpaRepository<AnswerSimilarityDetectionEvent, Long>,
        QuerydslPredicateExecutor<AnswerSimilarityDetectionEvent> {

  /**
   * Returns the detection event based on its id
   *
   * @param eventId the detection event id
   */
  AnswerSimilarityDetectionEvent findAnswerSimilarityEventById(@Param("eventId") Long eventId);

  /**
   * Returns all answer similarity detection events of cheating detection
   *
   * @param cheatingDetectionId the detection event id
   */
  List<AnswerSimilarityDetectionEvent> findAllByCheatingDetectionId(
      @Param("cheatingDetectionId") Long cheatingDetectionId);
}
