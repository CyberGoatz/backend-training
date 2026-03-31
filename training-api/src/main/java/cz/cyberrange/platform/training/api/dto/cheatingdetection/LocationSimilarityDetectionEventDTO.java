package cz.cyberrange.platform.training.api.dto.cheatingdetection;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
@ApiModel(
    value = "LocationSimilarityDetectionEventDTO",
    description = "A detection event of type Location Similarity.",
    parent = AbstractDetectionEventDTO.class)
public class LocationSimilarityDetectionEventDTO extends AbstractDetectionEventDTO {

  @ApiModelProperty(value = "Ip address of participant.", example = "1.1.1.1")
  private String ipAddress;

  @ApiModelProperty(value = "DNS of participant.", example = "dns.provider.cz")
  private String dns;

  @ApiModelProperty(value = "If the address is the same as deployment.", example = "false")
  private boolean isAddressDeploy;
}
