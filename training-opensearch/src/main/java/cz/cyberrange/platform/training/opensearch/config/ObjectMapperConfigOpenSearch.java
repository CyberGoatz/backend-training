package cz.cyberrange.platform.training.opensearch.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/** Configuration class for object mapper of OpenSearch logging. */
@Configuration
@ComponentScan(basePackages = {"cz.cyberrange.platform.training.opensearch.logging"})
public class ObjectMapperConfigOpenSearch {

  /**
   * Object mapper object mapper.
   *
   * @return the object mapper
   */
  @Bean("objMapperForOpenSearch")
  public ObjectMapper objectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    return objectMapper;
  }
}
