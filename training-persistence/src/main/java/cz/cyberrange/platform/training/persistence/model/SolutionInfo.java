package cz.cyberrange.platform.training.persistence.model;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Class represents information of hint associated with current level of training run */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class SolutionInfo {

  @Column(name = "training_level_id", nullable = false)
  private long trainingLevelId;

  @Column(name = "solution_content", nullable = false)
  private String solutionContent;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SolutionInfo that = (SolutionInfo) o;
    return trainingLevelId == that.trainingLevelId && solutionContent.equals(that.solutionContent);
  }

  @Override
  public int hashCode() {
    return Objects.hash(trainingLevelId, solutionContent);
  }

  @Override
  public String toString() {
    return "SolutionInfo{"
        + "trainingLevelId="
        + trainingLevelId
        + ", solutionContent='"
        + solutionContent
        + '\''
        + '}';
  }
}
