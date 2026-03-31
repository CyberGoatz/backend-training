package cz.cyberrange.platform.training.persistence.model;

import java.util.Objects;
import javax.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Class representing levels from Training definition. This class is extended by TrainingLevel,
 * InfoLevel and AssessmentLevel.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "abstract_level")
@Inheritance(strategy = InheritanceType.JOINED)
@NamedQueries({
  @NamedQuery(
      name = "AbstractLevel.findByIdIncludingDefinition",
      query =
          "SELECT l FROM AbstractLevel l "
              + "JOIN FETCH l.trainingDefinition td "
              + "JOIN FETCH td.authors "
              + "LEFT OUTER JOIN FETCH td.betaTestingGroup btg "
              + "LEFT OUTER JOIN FETCH btg.organizers "
              + "WHERE l.id = :levelId"),
  @NamedQuery(
      name = "AbstractLevel.getCurrentMaxOrder",
      query =
          "SELECT COALESCE(MAX(l.order), -1) FROM AbstractLevel l WHERE l.trainingDefinition.id = :trainingDefinitionId"),
  @NamedQuery(
      name = "AbstractLevel.findAllLevelsByTrainingDefinitionId",
      query =
          "SELECT l FROM AbstractLevel l WHERE l.trainingDefinition.id = :trainingDefinitionId ORDER BY l.order"),
  @NamedQuery(
      name = "AbstractLevel.findFirstLevelByTrainingDefinitionId",
      query =
          "SELECT l FROM AbstractLevel l WHERE l.trainingDefinition.id = :trainingDefinitionId ORDER BY l.order"),
  @NamedQuery(
      name = "AbstractLevel.findLevelInDefinition",
      query =
          "SELECT l FROM AbstractLevel l WHERE l.trainingDefinition.id = :trainingDefinitionId AND l.id = :levelId"),
  @NamedQuery(
      name = "AbstractLevel.increaseOrderOfLevels",
      query =
          "UPDATE AbstractLevel l SET l.order = l.order + 1 "
              + "WHERE l.trainingDefinition.id = :trainingDefinitionId AND l.order >= :fromOrder AND l.order <= :toOrder"),
  @NamedQuery(
      name = "AbstractLevel.decreaseOrderOfLevels",
      query =
          "UPDATE AbstractLevel l SET l.order = l.order - 1 "
              + "WHERE l.trainingDefinition.id = :trainingDefinitionId AND l.order >= :fromOrder AND l.order <= :toOrder")
})
public abstract class AbstractLevel extends AbstractEntity<Long> {

  @Column(name = "title", nullable = false)
  private String title;

  @Column(name = "max_score", nullable = false)
  private int maxScore;

  @Column(name = "estimated_duration")
  private long estimatedDuration;

  @Column(name = "minimal_possible_solve_time")
  private Long minimalPossibleSolveTime;

  @Column(name = "order_in_training_definition", nullable = false)
  private int order;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "training_definition_id")
  private TrainingDefinition trainingDefinition;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof AbstractLevel)) return false;
    AbstractLevel that = (AbstractLevel) o;
    return getMaxScore() == that.getMaxScore() && Objects.equals(getId(), that.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId(), getMaxScore());
  }

  @Override
  public String toString() {
    return "AbstractLevel{"
        + "id="
        + super.getId()
        + "title='"
        + title
        + '\''
        + ", maxScore="
        + maxScore
        + ", estimatedDuration="
        + estimatedDuration
        + ", order="
        + order
        + '}';
  }
}
