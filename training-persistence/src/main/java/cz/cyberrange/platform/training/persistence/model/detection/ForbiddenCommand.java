package cz.cyberrange.platform.training.persistence.model.detection;

import cz.cyberrange.platform.training.persistence.model.AbstractEntity;
import cz.cyberrange.platform.training.persistence.model.enums.CommandType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@ToString
@Entity
@Table(name = "forbidden_command")
public class ForbiddenCommand extends AbstractEntity<Long> {

  @Column(name = "command", nullable = false)
  private String command;

  @Column(name = "command_type", nullable = false)
  private CommandType type;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "cheating_detection_id")
  private CheatingDetection cheatingDetection;
}
