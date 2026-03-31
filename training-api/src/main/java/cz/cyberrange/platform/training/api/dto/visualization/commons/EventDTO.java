package cz.cyberrange.platform.training.api.dto.visualization.commons;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@EqualsAndHashCode
@Getter
@Setter
@NoArgsConstructor
public class EventDTO {

  private String text;
  private long time;
  private int score;

  public EventDTO(long time) {
    this.time = time;
  }
}
