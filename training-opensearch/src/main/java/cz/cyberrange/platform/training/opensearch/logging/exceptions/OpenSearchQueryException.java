package cz.cyberrange.platform.training.opensearch.logging.exceptions;

import java.io.IOException;

/** Exception thrown when an error occurs during querying OpenSearch. */
public class OpenSearchQueryException extends IOException {

  public OpenSearchQueryException(String message, Throwable cause) {
    super(message, cause);
  }
}
