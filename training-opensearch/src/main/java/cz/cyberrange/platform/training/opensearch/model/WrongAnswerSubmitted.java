package cz.cyberrange.platform.training.opensearch.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/** The type Wrong Answer Submitted. */
@SuperBuilder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Getter
@ToString
@JsonRootName("event")
public class WrongAnswerSubmitted extends AbstractAuditPOJO {

  @JsonProperty(value = "answer_content", required = true)
  @JsonAlias("flag_content")
  private String answerContent;

  // The number of wrong try (indicates the sequence number of the wrong answer)
  @JsonProperty(value = "count", required = true)
  private int count;
}
