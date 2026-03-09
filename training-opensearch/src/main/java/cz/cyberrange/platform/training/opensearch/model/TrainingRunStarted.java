package cz.cyberrange.platform.training.opensearch.model;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/** The type Training run started. */
@SuperBuilder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@ToString
@JsonRootName("event")
public class TrainingRunStarted extends AbstractAuditPOJO {}
