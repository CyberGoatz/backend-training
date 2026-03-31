package cz.cyberrange.platform.training.api.exceptions.errors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import cz.cyberrange.platform.training.api.exceptions.EntityErrorDetail;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.beans.ConstructorProperties;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.springframework.http.HttpStatus;

@ApiModel(
    value = "JavaApiError",
    description = "A detailed error from another Java mircorservice.",
    parent = ApiSubError.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class JavaApiError extends ApiSubError {
  @ApiModelProperty(
      value = "The time when the exception occurred",
      example = "1574062900 (different for each type of exception)")
  private long timestamp;

  @ApiModelProperty(
      value = "The specific description of the ApiError.",
      example =
          "The IDMGroup could not be found in database (different for each type of exception).")
  private String message;

  @ApiModelProperty(
      value = "The HTTP response status code",
      example = "404 Not found (different for each type of exception).")
  private HttpStatus status;

  @ApiModelProperty(
      value = "The list of main reasons of the ApiError.",
      example = "[The requested resource was not found (different for each type of exception).]")
  private List<String> errors;

  @ApiModelProperty(
      value = "The requested URI path which caused error.",
      example = "/user-and-group/api/v1/groups/1000 (different for each type of exception).")
  private String path;

  @ApiModelProperty(value = "Entity detail related to the error.")
  @JsonProperty("entity_error_detail")
  private EntityErrorDetail entityErrorDetail;

  @ConstructorProperties({"message"})
  private JavaApiError(String message) {
    this.message = message;
  }

  public static JavaApiError of(
      HttpStatus httpStatus, String message, List<String> errors, String path) {
    JavaApiError apiError = new JavaApiError(message);
    apiError.setStatus(httpStatus);
    apiError.setTimestamp(System.currentTimeMillis());
    apiError.setErrors(errors);
    apiError.setPath(path);
    return apiError;
  }

  public static JavaApiError of(HttpStatus httpStatus, String message, String error, String path) {
    JavaApiError apiError = new JavaApiError(message);
    apiError.setStatus(httpStatus);
    apiError.setTimestamp(System.currentTimeMillis());
    apiError.setError(error);
    apiError.setPath(path);
    return apiError;
  }

  public static JavaApiError of(HttpStatus httpStatus, String message, List<String> errors) {
    return JavaApiError.of(httpStatus, message, errors, "");
  }

  public static JavaApiError of(HttpStatus httpStatus, String message, String error) {
    return JavaApiError.of(httpStatus, message, error, "");
  }

  public static JavaApiError of(HttpStatus httpStatus, String message) {
    return JavaApiError.of(httpStatus, message, "", "");
  }

  public static JavaApiError of(String message) {
    return JavaApiError.of(null, message, "", "");
  }

  public EntityErrorDetail getEntityErrorDetail() {
    return entityErrorDetail;
  }

  public void setEntityErrorDetail(EntityErrorDetail entityErrorDetail) {
    this.entityErrorDetail = entityErrorDetail;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  @Override
  public String getMessage() {
    return message == null ? "No specific message provided." : message;
  }

  public void setMessage(final String message) {
    this.message = message;
  }

  public HttpStatus getStatus() {
    return status;
  }

  public void setStatus(HttpStatus status) {
    this.status = status;
  }

  public List<String> getErrors() {
    return errors;
  }

  public void setErrors(final List<String> errors) {
    this.errors = errors;
  }

  public void setError(final String error) {
    errors = Arrays.asList(error);
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  @Override
  public String toString() {
    return "ApiError{"
        + "timestamp="
        + timestamp
        + ", status="
        + getStatus()
        + ", message='"
        + message
        + '\''
        + ", errors="
        + errors
        + ", path='"
        + path
        + '\''
        + ", entityErrorDetail="
        + entityErrorDetail
        + '}';
  }

  @Override
  public int hashCode() {
    return Objects.hash(timestamp, getStatus(), message, errors, path);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (!(obj instanceof JavaApiError)) return false;
    JavaApiError other = (JavaApiError) obj;
    return Objects.equals(errors, other.getErrors())
        && Objects.equals(message, other.getMessage())
        && Objects.equals(path, other.getPath())
        && Objects.equals(getStatus(), other.getStatus())
        && Objects.equals(timestamp, other.getTimestamp());
  }
}
