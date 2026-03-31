package cz.cyberrange.platform.training.api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(
    value = HttpStatus.TOO_MANY_REQUESTS,
    reason = "The user has sent too many requests in a given amount of time (\"rate limiting\").")
public class TooManyRequestsException extends ExceptionWithEntity {

  public TooManyRequestsException() {
    super();
  }

  public TooManyRequestsException(EntityErrorDetail entityErrorDetail) {
    super(entityErrorDetail);
  }

  public TooManyRequestsException(EntityErrorDetail entityErrorDetail, Throwable cause) {
    super(entityErrorDetail, cause);
  }

  public TooManyRequestsException(Throwable cause) {
    super(cause);
  }

  protected String createDefaultReason(EntityErrorDetail entityErrorDetail) {
    StringBuilder reason =
        new StringBuilder("User has sent too many requests to obtain entity ")
            .append(entityErrorDetail.getEntity());
    if (entityErrorDetail.getIdentifier() != null
        && entityErrorDetail.getIdentifierValue() != null) {
      reason
          .append(" (")
          .append(entityErrorDetail.getIdentifier())
          .append(": ")
          .append(entityErrorDetail.getIdentifierValue())
          .append(")");
    }
    reason.append(".");
    return reason.toString();
  }
}
