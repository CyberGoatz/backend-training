package cz.cyberrange.platform.training.api.dto.visualization;

import cz.cyberrange.platform.training.api.dto.imports.AbstractLevelImportDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Encapsulates information about info level. Inherits from {@link AbstractLevelVisualizationDTO}
 * Used for visualization.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@ApiModel(
    value = "InfoLevelVisualizationDTO",
    description = "Information about info level needed for visualizations.",
    parent = AbstractLevelImportDTO.class)
public class InfoLevelVisualizationDTO extends AbstractLevelVisualizationDTO {

  @ApiModelProperty(
      value = "The information and experiences that are directed towards a participant.",
      example = "Informational stuff")
  private String content;
}
