package cz.cyberrange.platform.training.persistence.model.question;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class QuestionAnswerId implements Serializable {

  @Column(name = "question_id")
  private Long questionId;

  @Column(name = "training_run_id")
  private Long trainingRunId;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof QuestionAnswerId)) return false;
    QuestionAnswerId that = (QuestionAnswerId) o;
    return Objects.equals(getQuestionId(), that.getQuestionId())
        && Objects.equals(getTrainingRunId(), that.getTrainingRunId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getQuestionId(), getTrainingRunId());
  }

  @Override
  public String toString() {
    return "QuestionAnswerId{"
        + "questionId="
        + this.getQuestionId()
        + ", trainingRunId="
        + this.getTrainingRunId()
        + '}';
  }
}
