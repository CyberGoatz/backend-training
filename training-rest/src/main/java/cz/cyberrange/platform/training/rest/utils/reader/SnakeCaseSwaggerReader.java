package cz.cyberrange.platform.training.rest.utils.reader;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.github.kongchen.swagger.docgen.reader.SpringMvcApiReader;
import io.swagger.models.Swagger;
import io.swagger.util.Json;
import org.apache.maven.plugin.logging.Log;

/**
 * SnakeCaseSwaggerReader extends SpringMvcApiReader which is able to read SpringMVC annotations and
 * is used to generate documentation using kongchen plugin. Class overrides the default property
 * naming strategy by snake case strategy. See configuration of the kongchen plugin in the pom.xml
 * file.
 */
public class SnakeCaseSwaggerReader extends SpringMvcApiReader {

  public SnakeCaseSwaggerReader(Swagger swagger, Log log) {
    super(swagger, log);
    Json.mapper().setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());
  }
}
