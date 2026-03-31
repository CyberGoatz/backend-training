package cz.cyberrange.platform.training.api.dto.export;

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
@ApiModel(value = "AttachmentExportDTO", description = "An exported attachment of training level.")
public class AttachmentExportDTO {

  @ApiModelProperty(value = "URL link to file or website.")
  private String content;
}
