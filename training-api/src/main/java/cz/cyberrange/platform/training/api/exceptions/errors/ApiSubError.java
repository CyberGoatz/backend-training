package cz.cyberrange.platform.training.api.exceptions.errors;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import io.swagger.annotations.ApiModel;

@ApiModel(
    value = "ApiSubError",
    subTypes = {JavaApiError.class, PythonApiError.class},
    description = "Superclass for classes JavaApiError and PythonApiError")
@JsonSubTypes({
  @JsonSubTypes.Type(value = JavaApiError.class, name = "JavaApiError"),
  @JsonSubTypes.Type(value = PythonApiError.class, name = "PythonApiError")
})
public abstract class ApiSubError {

  public abstract String getMessage();
}
