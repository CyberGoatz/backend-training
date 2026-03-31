package cz.cyberrange.platform.training.api.dto.archive;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class QuestionEMIAnswer {

  @JsonProperty("statementOrder")
  private Integer statementOrder;

  @JsonProperty("optionOrder")
  private Integer optionOrder;
}
