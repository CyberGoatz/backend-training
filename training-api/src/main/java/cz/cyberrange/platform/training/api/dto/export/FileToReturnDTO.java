package cz.cyberrange.platform.training.api.dto.export;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/** Class encapsulating entity into file. */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(
    value = "FileToReturnDTO",
    description = "Wrapping model which contains the content and title of the file.")
public class FileToReturnDTO {

  @ApiModelProperty(value = "Content of the file.", example = "[string]")
  private byte[] content;

  @ApiModelProperty(value = "Title of the file.", example = "TrainingInstance-NetworkDemolition")
  private String title;
}
