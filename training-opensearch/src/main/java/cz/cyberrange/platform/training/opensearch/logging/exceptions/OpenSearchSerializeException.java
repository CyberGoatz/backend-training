package cz.cyberrange.platform.training.opensearch.logging.exceptions;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Exception thrown when an error occurs during serialization of log event or query from OpenSearch.
 */
public class OpenSearchSerializeException extends JsonProcessingException {

  /**
   * Instantiates a new OpenSearchSerializeException
   *
   * @param message the message
   */
  public OpenSearchSerializeException(String message) {
    super(message);
  }

  /**
   * Instantiates a new OpenSearchSerializeException
   *
   * @param message the message
   * @param ex the exception
   */
  public OpenSearchSerializeException(String message, Throwable ex) {
    super(message, ex);
  }

  /**
   * Instantiates a new OpenSearchSerializeException
   *
   * @param ex the exception
   */
  public OpenSearchSerializeException(Throwable ex) {
    super(ex);
  }
}
