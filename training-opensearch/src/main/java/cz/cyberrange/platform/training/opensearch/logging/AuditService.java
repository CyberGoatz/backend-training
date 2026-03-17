package cz.cyberrange.platform.training.opensearch.logging;

import static org.springframework.util.Assert.notNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.cyberrange.platform.training.opensearch.logging.exceptions.OpenSearchSerializeException;
import cz.cyberrange.platform.training.opensearch.model.AbstractAuditPOJO;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

/** The type Audit service. */
@Service
public class AuditService {

  private static final Logger logger = LoggerFactory.getLogger(AuditService.class);

  private static final Long DEFAULT_TIMESTAMP_DELAY_MS = 10L;

  private final ObjectMapper objectMapper;

  /**
   * Instantiates a new Audit service.
   *
   * @param objectMapper the object mapper
   */
  @Autowired
  public AuditService(@Qualifier("objMapperForOpenSearch") ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  /**
   * Method for saving general class into OpenSearch under specific index and type.
   *
   * @param <T> the type parameter of the class to be saved, must extend {@link AbstractAuditPOJO}
   * @param pojoClass class saved to OpenSearch
   * @param priority used to delay the timestamp of the log entry, so that the priority of events is
   *     properly reflected in OpenSearch logs (e.g., level answer event should have priority 0,
   *     level complete which is logged immediately after level answer should have priority 1)
   * @throws OpenSearchSerializeException exception when writing to OpenSearch logs fails
   */
  @SneakyThrows
  public <T extends AbstractAuditPOJO> void saveTrainingRunEvent(
      @NonNull T pojoClass, int priority) {
    if (priority < 0) {
      throw new IllegalArgumentException("Order must be non-negative");
    }
    notNull(pojoClass, "Null class could not be saved via audit method.");
    try {
      pojoClass.setTimestamp(
          System.currentTimeMillis() + (long) priority * DEFAULT_TIMESTAMP_DELAY_MS);
      pojoClass.setType(pojoClass.getClass().getName());

      logger.info(objectMapper.writeValueAsString(pojoClass));
    } catch (JsonProcessingException e) {
      logger.error("Failed to serialize audit log entry to JSON", e);
      throw new OpenSearchSerializeException("Failed to serialize audit log entry to JSON", e);
    }
  }

  /**
   * Method for saving general class into OpenSearch under specific index and type. Highest priority
   * is used (see {@link #saveTrainingRunEvent(AbstractAuditPOJO, int)})
   *
   * @param <T> the type parameter of the class to be saved, must extend {@link AbstractAuditPOJO}
   * @param pojoClass class saved to OpenSearch
   * @throws OpenSearchSerializeException exception when writing to OpenSearch logs fails
   */
  @SneakyThrows
  public <T extends AbstractAuditPOJO> void saveTrainingRunEvent(@NonNull T pojoClass) {
    saveTrainingRunEvent(pojoClass, 0);
  }
}
