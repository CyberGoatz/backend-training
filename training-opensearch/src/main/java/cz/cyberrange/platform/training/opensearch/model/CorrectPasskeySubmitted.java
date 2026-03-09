package cz.cyberrange.platform.training.opensearch.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;


/** The type Correct Passkey Submitted. */
@SuperBuilder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Getter
@ToString
@JsonRootName("event")
public class CorrectPasskeySubmitted extends AbstractAuditPOJO {

  @JsonProperty(value = "passkey_content", required = true)
  private String passkeyContent;
}
