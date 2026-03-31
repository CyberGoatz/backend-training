package cz.cyberrange.platform.training.persistence.model.question;

import cz.cyberrange.platform.training.persistence.model.AssessmentLevel;
import cz.cyberrange.platform.training.persistence.model.enums.QuestionType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "question")
public class Question implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "questionGenerator")
  @SequenceGenerator(name = "questionGenerator", sequenceName = "question_seq")
  @Column(name = "question_id", nullable = false, unique = true)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(name = "question_type")
  private QuestionType questionType;

  @Column(name = "text")
  private String text;

  @Column(name = "order_in_assessment")
  private int order;

  @Column(name = "points")
  private int points;

  @Column(name = "penalty")
  private int penalty;

  @Column(name = "answer_required")
  private boolean answerRequired;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "assessment_level_id")
  private AssessmentLevel assessmentLevel;

  @OrderBy("order asc")
  @OneToMany(
      mappedBy = "question",
      cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  private List<QuestionChoice> choices = new ArrayList<>();

  @OrderBy("order asc")
  @OneToMany(
      mappedBy = "question",
      cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  private List<ExtendedMatchingStatement> extendedMatchingStatements = new ArrayList<>();

  @OrderBy("order asc")
  @OneToMany(
      mappedBy = "question",
      cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  private List<ExtendedMatchingOption> extendedMatchingOptions = new ArrayList<>();

  public void setChoices(List<QuestionChoice> questionChoices) {
    this.choices = questionChoices;
    this.choices.forEach(choice -> choice.setQuestion(this));
  }

  public List<ExtendedMatchingStatement> getExtendedMatchingStatements() {
    return extendedMatchingStatements;
  }

  public void setExtendedMatchingStatements(
      List<ExtendedMatchingStatement> extendedMatchingStatements) {
    this.extendedMatchingStatements = extendedMatchingStatements;
    this.extendedMatchingStatements.forEach(statement -> statement.setQuestion(this));
  }

  public List<ExtendedMatchingOption> getExtendedMatchingOptions() {
    return extendedMatchingOptions;
  }

  public void setExtendedMatchingOptions(List<ExtendedMatchingOption> extendedMatchingOptions) {
    this.extendedMatchingOptions = extendedMatchingOptions;
    this.extendedMatchingOptions.forEach(option -> option.setQuestion(this));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Question)) return false;
    Question question = (Question) o;
    return getOrder() == question.getOrder()
        && getQuestionType() == question.getQuestionType()
        && Objects.equals(getText(), question.getText());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getQuestionType(), getText(), getOrder());
  }

  @Override
  public String toString() {
    return "Question{"
        + "id="
        + this.getId()
        + ", questionType="
        + this.getQuestionType()
        + ", text='"
        + this.getText()
        + '\''
        + ", order="
        + this.getOrder()
        + '}';
  }
}
