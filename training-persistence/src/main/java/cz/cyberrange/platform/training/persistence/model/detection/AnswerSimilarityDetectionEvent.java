package cz.cyberrange.platform.training.persistence.model.detection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@ToString
@Entity
@Table(name = "answer_similarity_detection_event")
@PrimaryKeyJoinColumn(name = "id")
@NamedQueries({
  @NamedQuery(
      name = "AnswerSimilarityDetectionEvent.findAnswerSimilarityEventById",
      query = "SELECT asde FROM AnswerSimilarityDetectionEvent asde WHERE asde.id = :eventId"),
  @NamedQuery(
      name = "AnswerSimilarityDetectionEvent.findAllByCheatingDetectionId",
      query =
          "SELECT asde FROM AnswerSimilarityDetectionEvent asde WHERE asde.cheatingDetectionId = :cheatingDetectionId")
})
public class AnswerSimilarityDetectionEvent extends AbstractDetectionEvent {

  @Column(name = "answer")
  private String answer;

  @Column(name = "answer_owner", nullable = false)
  private String answerOwner;
}
