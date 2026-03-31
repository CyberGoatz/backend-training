package cz.cyberrange.platform.training.api.dto.export;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/** Encapsulates information about user reference. */
@Getter
@Setter
@ToString
@ApiModel(value = "UserRefExportDTO", description = "An exported information about user reference.")
public class UserRefExportDTO {

  @ApiModelProperty(
      value = "Reference to user in another microservice.",
      example = "999999@mail.example.cz")
  private String userRefLogin;

  @ApiModelProperty(
      value = "Reference to user in another microservice and get his full name",
      example = "Mgr. John Doe")
  private String userRefFullName;

  @ApiModelProperty(value = "User given name", example = "John")
  private String userRefGivenName;

  @ApiModelProperty(value = "User family name", example = "Doe")
  private String userRefFamilyName;

  @ApiModelProperty(
      value = "Reference to user in another microservice and get his iss",
      example = "https://oidc.provider.cz")
  private String iss;

  @ApiModelProperty(
      value = "Reference to user in another microservice and get his id",
      example = "1")
  private Long userRefId;
}
