package cz.cyberrange.platform.training.api.dto.export;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/** Encapsulates information about info level. Inherits from {@link AbstractLevelExportDTO} */
@Getter
@Setter
@ToString
@ApiModel(
    value = "InfoLevelExportDTO",
    description = "Exported info level.",
    parent = AbstractLevelExportDTO.class)
public class InfoLevelExportDTO extends AbstractLevelExportDTO {

  @ApiModelProperty(
      value = "The information and experiences that are directed towards a participant.",
      example = "Informational stuff")
  private String content;

  /** Instantiates a new Info level export dto. */
  public InfoLevelExportDTO() {
    this.content = "";
  }
}
