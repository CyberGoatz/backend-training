package cz.cyberrange.platform.training.api.dto.visualization.leveltabs;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@EqualsAndHashCode
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class LevelTabsHintDTO {

  private Long id;
  private int order;
  private String title;
  private int penalty;
}
