package cz.cyberrange.platform.training.opensearch.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/** The type Training run ended. */
@SuperBuilder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Getter
@ToString
@JsonRootName("event")
public class TrainingRunEnded extends AbstractAuditPOJO {

  // Training run start time
  @JsonProperty(value = "start_time", required = true)
  private long startTime;

  // Training run end time
  @JsonProperty(value = "end_time", required = true)
  private long endTime;
}
