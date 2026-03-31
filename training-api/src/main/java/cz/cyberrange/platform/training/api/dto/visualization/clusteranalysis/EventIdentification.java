package cz.cyberrange.platform.training.api.dto.visualization.clusteranalysis;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventIdentification {

  private Long trainingDefinitionId;
  private Long trainingInstanceId;
  private Long trainingRunId;
}
