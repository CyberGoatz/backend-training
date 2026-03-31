package cz.cyberrange.platform.training.api.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/** Encapsulates information used for auditing. */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class AuditInfoDTO {

  private long userRefId;
  private long sandboxId;
  private long poolId;
  private long trainingRunId;
  private long trainingDefinitionId;
  private long trainingInstanceId;
  private long trainingTime;
  private long level;
  private int totalScore;
  private int actualScoreInLevel;
}
