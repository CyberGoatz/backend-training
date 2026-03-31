package cz.cyberrange.platform.training.api.dto.cheatingdetection;

import cz.cyberrange.platform.training.api.enums.CommandType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@ApiModel(value = "ForbiddenCommandDTO", description = "Basic information about forbidden command.")
public class ForbiddenCommandDTO {

  @ApiModelProperty(value = "command.", example = "nmap")
  private String command;

  @ApiModelProperty(value = "Type of command.", example = "BASH")
  private CommandType type;

  @ApiModelProperty(value = "Id of cheating detection.", example = "1")
  private Long cheatingDetectionId;
}
