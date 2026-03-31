package cz.cyberrange.platform.training.api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(
    value = HttpStatus.FORBIDDEN,
    reason = "Request is formed correctly, but the server doesn't want to carry it out.")
public class ForbiddenException extends RuntimeException {

  public ForbiddenException() {}

  public ForbiddenException(String message) {
    super(message);
  }

  public ForbiddenException(String message, Throwable ex) {
    super(message, ex);
  }

  public ForbiddenException(Throwable ex) {
    super(ex);
  }
}
