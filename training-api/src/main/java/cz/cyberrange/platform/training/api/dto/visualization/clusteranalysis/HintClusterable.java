package cz.cyberrange.platform.training.api.dto.visualization.clusteranalysis;

import cz.cyberrange.platform.training.api.utils.ClusterMathUtils;
import java.util.Collection;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.math3.stat.clustering.Clusterable;

@Getter
@Setter
public class HintClusterable implements Clusterable<HintClusterable> {

  private final Long userRefId;
  private final Long level;
  private Double timeSpentAfterHint;
  private Double wrongAnswersAfterHint;
  private Double timeSpentAfterHintNormalized;
  private Double wrongAnswersAfterHintNormalized;

  public HintClusterable(
      Long userRefId, Long level, Double timeSpentAfterHint, Double wrongAnswersAfterHint) {
    this.userRefId = userRefId;
    this.level = level;
    this.timeSpentAfterHint = timeSpentAfterHint;
    this.wrongAnswersAfterHint = wrongAnswersAfterHint;
    this.timeSpentAfterHintNormalized = timeSpentAfterHint;
    this.wrongAnswersAfterHintNormalized = wrongAnswersAfterHint;
  }

  @Override
  public double distanceFrom(HintClusterable p) {
    return ClusterMathUtils.calculateDistance2D(
        timeSpentAfterHintNormalized,
        p.getTimeSpentAfterHintNormalized(),
        wrongAnswersAfterHintNormalized,
        p.getWrongAnswersAfterHintNormalized());
  }

  @Override
  public HintClusterable centroidOf(Collection<HintClusterable> p) {
    return new HintClusterable(
        0L, 0L, computeTimeSpentAfterHint(p), computeWrongAnswersAfterHint(p));
  }

  private Double computeTimeSpentAfterHint(Collection<HintClusterable> p) {
    return p.stream()
        .reduce(
            0.0,
            (value, featureOne) -> value + featureOne.getTimeSpentAfterHintNormalized() / p.size(),
            Double::sum);
  }

  private Double computeWrongAnswersAfterHint(Collection<HintClusterable> p) {
    return p.stream()
        .reduce(
            0.0,
            (value, featureOne) ->
                value + featureOne.getWrongAnswersAfterHintNormalized() / p.size(),
            Double::sum);
  }
}
