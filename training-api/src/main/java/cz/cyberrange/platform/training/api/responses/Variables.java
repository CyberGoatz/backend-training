package cz.cyberrange.platform.training.api.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import java.util.Set;
import lombok.Data;

@Data
public class Variables {
  @ApiModelProperty(
      value = "Variables associated with sandbox definition of the pool",
      example = "['secret', 'port']")
  @JsonProperty("variables")
  private Set<String> variables;
}
