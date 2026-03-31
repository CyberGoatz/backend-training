package cz.cyberrange.platform.training.persistence.model;

import cz.cyberrange.platform.training.persistence.model.enums.AssessmentType;
import cz.cyberrange.platform.training.persistence.model.question.Question;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Class specifying Abstract level as assessment level. Assessment levels contain questions for
 * trainees to answer.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "assessment_level")
@PrimaryKeyJoinColumn(name = "id")
public class AssessmentLevel extends AbstractLevel {

  @OrderBy("order asc")
  @OneToMany(
      mappedBy = "assessmentLevel",
      cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  private List<Question> questions = new ArrayList<>();

  @Column(name = "instructions", nullable = false)
  private String instructions;

  @Column(name = "assessment_type", length = 128, nullable = false)
  @Enumerated(EnumType.STRING)
  private AssessmentType assessmentType;

  /**
   * Sets questions that trainee must answer
   *
   * @param questions the questions
   */
  public void setQuestions(List<Question> questions) {
    this.questions = questions;
    this.questions.forEach(question -> question.setAssessmentLevel(this));
    this.questions.sort(Comparator.comparingInt(Question::getOrder));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof AssessmentLevel)) return false;
    if (!super.equals(o)) return false;
    AssessmentLevel that = (AssessmentLevel) o;
    return Objects.equals(getQuestions(), that.getQuestions())
        && Objects.equals(getInstructions(), that.getInstructions())
        && getAssessmentType() == that.getAssessmentType();
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), getQuestions(), getInstructions(), getAssessmentType());
  }

  @Override
  public String toString() {
    return "AssessmentLevel{"
        + "questions='"
        + questions
        + '\''
        + ", instructions='"
        + instructions
        + '\''
        + ", assessmentType="
        + assessmentType
        + '}';
  }
}
