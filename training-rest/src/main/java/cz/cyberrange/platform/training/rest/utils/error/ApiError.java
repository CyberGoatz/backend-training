package cz.cyberrange.platform.training.rest.utils.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.springframework.http.HttpStatus;

/** The type Api error. */
@ApiModel(
    value = "ApiError",
    subTypes = {ApiEntityError.class, ApiMicroserviceError.class},
    description = "Superclass for classes ApiEntityError and ApiMicroserviceError")
@JsonSubTypes({
  @JsonSubTypes.Type(value = ApiEntityError.class, name = "ApiEntityError"),
  @JsonSubTypes.Type(value = ApiMicroserviceError.class, name = "ApiMicroserviceError")
})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {

  @ApiModelProperty(
      value = "The time when the exception occurred",
      example = "1574062900 (different for each type of exception)")
  private long timestamp;

  @ApiModelProperty(
      value = "The HTTP response status code",
      example = "404 Not found (different for each type of exception).")
  private HttpStatus status;

  @ApiModelProperty(
      value = "The specific description of the ApiError.",
      example =
          "The IDMGroup could not be found in database (different for each type of exception).")
  private String message;

  @ApiModelProperty(
      value = "The list of main reasons of the ApiError.",
      example = "[The requested resource was not found (different for each type of exception).]")
  private List<String> errors;

  @ApiModelProperty(
      value = "The requested URI path which caused error.",
      example = "/user-and-group/api/v1/groups/1000 (different for each type of exception).")
  private String path;

  protected ApiError() {}

  private ApiError(HttpStatus httpStatus, String message, String path) {
    this.status = httpStatus;
    this.message = message;
    this.path = path;
    this.timestamp = System.currentTimeMillis();
  }

  /**
   * Of api error.
   *
   * @param httpStatus the http status
   * @param message the message
   * @param errors the errors
   * @param path the path
   * @return the api error
   */
  public static ApiError of(
      HttpStatus httpStatus, String message, List<String> errors, String path) {
    ApiError apiError = new ApiError(httpStatus, message, path);
    apiError.setErrors(errors);
    return apiError;
  }

  /**
   * Of api error.
   *
   * @param httpStatus the http status
   * @param message the message
   * @param error the error
   * @param path the path
   * @return the api error
   */
  public static ApiError of(HttpStatus httpStatus, String message, String error, String path) {
    ApiError apiError = new ApiError(httpStatus, message, path);
    apiError.setError(error);
    return apiError;
  }

  /**
   * Of api error.
   *
   * @param httpStatus the http status
   * @param message the message
   * @param errors the errors
   * @return the api error
   */
  public static ApiError of(HttpStatus httpStatus, String message, List<String> errors) {
    return ApiError.of(httpStatus, message, errors, "");
  }

  /**
   * Of api error.
   *
   * @param httpStatus the http status
   * @param message the message
   * @param error the error
   * @return the api error
   */
  public static ApiError of(HttpStatus httpStatus, String message, String error) {
    return ApiError.of(httpStatus, message, error, "");
  }

  /**
   * Gets timestamp.
   *
   * @return the timestamp
   */
  public long getTimestamp() {
    return timestamp;
  }

  /**
   * Sets timestamp.
   *
   * @param timestamp the timestamp
   */
  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  /**
   * Gets status.
   *
   * @return the status
   */
  public HttpStatus getStatus() {
    return status;
  }

  /**
   * Sets status.
   *
   * @param status the status
   */
  public void setStatus(final HttpStatus status) {
    this.status = status;
  }

  /**
   * Gets message.
   *
   * @return the message
   */
  public String getMessage() {
    return message;
  }

  /**
   * Sets message.
   *
   * @param message the message
   */
  public void setMessage(final String message) {
    this.message = message;
  }

  /**
   * Gets errors.
   *
   * @return the errors
   */
  public List<String> getErrors() {
    return errors;
  }

  /**
   * Sets errors.
   *
   * @param errors the errors
   */
  public void setErrors(final List<String> errors) {
    this.errors = errors;
  }

  /**
   * Sets error.
   *
   * @param error the error
   */
  public void setError(final String error) {
    errors = Arrays.asList(error);
  }

  /**
   * Gets path.
   *
   * @return the path
   */
  public String getPath() {
    return path;
  }

  /**
   * Sets path.
   *
   * @param path the path
   */
  public void setPath(String path) {
    this.path = path;
  }

  @Override
  public String toString() {
    return "ApiError{"
        + "timestamp="
        + timestamp
        + ", status="
        + status
        + ", message='"
        + message
        + '\''
        + ", errors="
        + errors
        + ", path='"
        + path
        + '\''
        + '}';
  }

  @Override
  public int hashCode() {
    return Objects.hash(timestamp, status, message, errors, path);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (!(obj instanceof ApiError)) return false;
    ApiError other = (ApiError) obj;
    return Objects.equals(errors, other.getErrors())
        && Objects.equals(message, other.getMessage())
        && Objects.equals(path, other.getPath())
        && Objects.equals(status, other.getStatus())
        && Objects.equals(timestamp, other.getTimestamp());
  }
}
