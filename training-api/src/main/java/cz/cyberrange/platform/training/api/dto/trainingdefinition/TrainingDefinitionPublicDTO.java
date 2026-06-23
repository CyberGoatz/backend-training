package cz.cyberrange.platform.training.api.dto.trainingdefinition;

import cz.cyberrange.platform.training.api.dto.BasicLevelInfoDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates learner-facing information about a Training Definition.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@ApiModel(value = "TrainingDefinitionPublicDTO", description = "Learner-facing training definition information.")
public class TrainingDefinitionPublicDTO {

    @ApiModelProperty(value = "Main identifier of training definition.", example = "1")
    private Long id;
    @ApiModelProperty(value = "A name of the training/game.", example = "Photo Hunter")
    private String title;
    @ApiModelProperty(value = "Description of training definition that is visible to the participant.")
    private String description;
    @ApiModelProperty(value = "List of knowledge and skills necessary to complete the training.")
    private String[] prerequisites;
    @ApiModelProperty(value = "A list of knowledge and skills that the participant should learn.")
    private String[] outcomes;
    @ApiModelProperty(value = "Estimated time it takes to finish runs created from this definition.", example = "5")
    private long estimatedDuration;
    @ApiModelProperty(value = "Basic information about levels in the training definition.")
    private List<BasicLevelInfoDTO> levels = new ArrayList<>();
}
