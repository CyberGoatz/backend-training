package cz.cyberrange.platform.training.rest.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Supported media types for .yml files -> https://stackoverflow.com/a/38000954/2892314 */
@Configuration
public class WebConfigRestTraining implements WebMvcConfigurer {

  private static final MediaType MEDIA_TYPE_YAML = MediaType.valueOf("text/yaml");
  private static final MediaType MEDIA_TYPE_YML = MediaType.valueOf("text/yml");

  @Autowired private ObjectMapper objectMapper;

  @Override
  public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
    configurer
        .favorPathExtension(true)
        .favorParameter(false)
        .ignoreAcceptHeader(false)
        .defaultContentType(MediaType.APPLICATION_JSON)
        .mediaType(MediaType.APPLICATION_JSON.getSubtype(), MediaType.APPLICATION_JSON)
        .mediaType(MEDIA_TYPE_YML.getSubtype(), MEDIA_TYPE_YML)
        .mediaType(MEDIA_TYPE_YAML.getSubtype(), MEDIA_TYPE_YAML);
  }

  @Override
  public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
    MappingJackson2HttpMessageConverter yamlConverter =
        new MappingJackson2HttpMessageConverter(objectMapper);
    yamlConverter.setSupportedMediaTypes(List.of(MEDIA_TYPE_YAML, MEDIA_TYPE_YML));
    converters.add(yamlConverter);
  }
}
