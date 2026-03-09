package cz.cyberrange.platform.training.opensearch.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/** The type Solution displayed. */
@SuperBuilder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Getter
@ToString
@JsonRootName("event")
public class SolutionDisplayed extends AbstractAuditPOJO {

  /** Solution displayed penalty points */
  @JsonProperty(value = "penalty_points", required = true)
  private int penaltyPoints;
}
