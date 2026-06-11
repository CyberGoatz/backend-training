package cz.cyberrange.platform.training.api.dto.run;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Public-safe aggregate training completion metrics for a user.
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
@ApiModel(value = "PublicTrainingSummaryDTO", description = "Public-safe aggregate training completion metrics.")
public class PublicTrainingSummaryDTO {

    @ApiModelProperty(value = "Number of completed trainings.", example = "12")
    private long completedCount;
    @ApiModelProperty(value = "Total public score from completed trainings.", example = "4200")
    private long totalScore;
    @ApiModelProperty(value = "Average public score from completed trainings.", example = "350")
    private long averageScore;
}
