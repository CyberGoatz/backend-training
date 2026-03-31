package cz.cyberrange.platform.training.rest.controllers.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import cz.cyberrange.platform.training.api.converters.LocalDateTimeDeserializer;
import cz.cyberrange.platform.training.api.converters.LocalDateTimeUTCDeserializer;
import cz.cyberrange.platform.training.api.converters.LocalDateTimeUTCSerializer;
import cz.cyberrange.platform.training.service.mapping.modelmapper.BeanMapping;
import cz.cyberrange.platform.training.service.mapping.modelmapper.BeanMappingImpl;
import java.io.IOException;
import java.time.LocalDateTime;
import org.modelmapper.ModelMapper;

public class ObjectConverter {

  private static BeanMapping beanMapping = new BeanMappingImpl(new ModelMapper());

  public static String convertObjectToJsonBytes(Object object) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule().addSerializer(new LocalDateTimeUTCSerializer()));
    mapper.setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());
    return mapper.writeValueAsString(object);
  }

  public static String convertJsonBytesToObject(String object) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(
        new JavaTimeModule().addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer()));
    mapper.setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());
    return mapper.readValue(object, String.class);
  }

  public static <T> T convertJsonBytesToObject(String object, TypeReference<T> tTypeReference)
      throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    mapper
        .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    mapper.setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());
    return mapper.readValue(object, tTypeReference);
  }

  public static <T> T convertJsonBytesToObject(String object, Class<T> objectClass)
      throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    mapper.registerModule(
        new JavaTimeModule()
            .addDeserializer(LocalDateTime.class, new LocalDateTimeUTCDeserializer()));
    mapper.setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());
    return mapper.readValue(object, objectClass);
  }

  public static String getInitialExceptionMessage(Exception exception) {
    while (exception.getCause() != null) {
      exception = (Exception) exception.getCause();
    }
    return exception.getMessage();
  }
}
