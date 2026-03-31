package cz.cyberrange.platform.training.api.dto.visualization.timeline;

import cz.cyberrange.platform.training.api.dto.visualization.commons.PlayerDataDTO;
import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@EqualsAndHashCode
@Getter
@Setter
@ToString
public class TimelineDTO {

  private long estimatedTime;
  private List<Integer> maxScoreOfLevels = new ArrayList<>();
  private long maxParticipantTime;
  private float averageTime;
  private List<PlayerDataDTO> playerData = new ArrayList<>();

  public void addPlayerData(PlayerDataDTO playerDataDTO) {
    this.playerData.add(playerDataDTO);
  }
}
