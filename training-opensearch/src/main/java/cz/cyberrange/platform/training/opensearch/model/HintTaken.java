package cz.cyberrange.platform.training.opensearch.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/** The type Hint taken. */
@SuperBuilder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Getter
@ToString
@JsonRootName("event")
public class HintTaken extends AbstractAuditPOJO {

  @JsonProperty(value = "hint_id", required = true)
  private long hintId;

  @JsonProperty(value = "hint_penalty_points", required = true)
  private int hintPenaltyPoints;

  @JsonProperty(value = "hint_title", required = true)
  private String hintTitle;
}
