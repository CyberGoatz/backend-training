package cz.cyberrange.platform.training.service.startup;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import cz.cyberrange.platform.training.api.exceptions.InternalServerErrorException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DefaultLevelsLoader {

  @Value("${path.to.default.levels:}")
  private String pathToDefaultLevels;

  private DefaultLevels defaultLevels;
  private final Validator validator;

  @Autowired
  public DefaultLevelsLoader(Validator validator) {
    this.validator = validator;
  }

  @PostConstruct
  private void loadDefaultLevels() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
    mapper.configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, true);
    mapper.setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());
    try {
      InputStream inputStream =
          pathToDefaultLevels.isBlank()
              ? getClass().getResourceAsStream("/default-levels.json")
              : new FileInputStream(pathToDefaultLevels);
      defaultLevels = mapper.readValue(inputStream, DefaultLevels.class);
      Set<ConstraintViolation<DefaultLevels>> violations = this.validator.validate(defaultLevels);
      if (!violations.isEmpty()) {
        throw new InternalServerErrorException(
            "Could not load the default phases. Reason: "
                + violations.stream().map(ConstraintViolation::getMessage).toList());
      }
    } catch (IOException e) {
      throw new InternalServerErrorException("Could not load file with the default levels.", e);
    }
  }

  public DefaultAccessLevel getDefaultAccessLevel() {
    return this.defaultLevels.getDefaultAccessLevel();
  }

  public DefaultInfoLevel getDefaultInfoLevel() {
    return this.defaultLevels.getDefaultInfoLevel();
  }
}
