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
import org.springframework.stereotype.Service;

/** The type Audit service. */
@Service
public class AuditService {

  private static Logger logger = LoggerFactory.getLogger(AuditService.class);

  private ObjectMapper objectMapper;

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
   * @param <T> the type parameter
   * @param pojoClass class saving to OpenSearch
   * @param timestampDelay delay in milliseconds to be added to the current timestamp
   * @throws OpenSearchSerializeException exception when writing to OpenSearch logs fails
   */
  @SneakyThrows
  public <T extends AbstractAuditPOJO> void saveTrainingRunEvent(T pojoClass, long timestampDelay) {
    notNull(pojoClass, "Null class could not be saved via audit method.");
    try {
      pojoClass.setTimestamp(System.currentTimeMillis() + timestampDelay);
      pojoClass.setType(pojoClass.getClass().getName());

      logger.info(objectMapper.writeValueAsString(pojoClass));
    } catch (JsonProcessingException e) {
      logger.error("Failed to serialize audit log entry to JSON", e);
      throw new OpenSearchSerializeException("Failed to serialize audit log entry to JSON", e);
    }
  }
}
