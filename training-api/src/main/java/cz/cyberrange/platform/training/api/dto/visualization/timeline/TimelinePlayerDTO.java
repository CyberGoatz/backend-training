package cz.cyberrange.platform.training.api.dto.visualization.timeline;

import cz.cyberrange.platform.training.api.dto.UserRefDTO;
import cz.cyberrange.platform.training.api.dto.visualization.commons.PlayerDataWithScoreDTO;
import cz.cyberrange.platform.training.api.dto.visualization.commons.VisualizationAbstractLevelDTO;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TimelinePlayerDTO extends PlayerDataWithScoreDTO {

  private List<VisualizationAbstractLevelDTO> levels = new ArrayList<>();

  public TimelinePlayerDTO(
      Long id,
      String name,
      byte[] picture,
      Long trainingRunId,
      Integer trainingScore,
      Integer assessmentScore) {
    super(id, name, picture, trainingRunId, trainingScore, assessmentScore);
  }

  public TimelinePlayerDTO(
      UserRefDTO userRef, Long trainingRunId, Integer trainingScore, Integer assessmentScore) {
    super(
        userRef.getUserRefId(),
        userRef.getUserRefFullName(),
        userRef.getPicture(),
        trainingRunId,
        trainingScore,
        assessmentScore);
  }

  public void addTableLevel(VisualizationAbstractLevelDTO visualizationAbstractLevelDTO) {
    this.levels.add(visualizationAbstractLevelDTO);
  }
}
