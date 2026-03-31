package cz.cyberrange.platform.training.persistence.converters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import cz.cyberrange.platform.training.persistence.model.ReferenceSolutionNode;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.AttributeConverter;

public class ReferenceSolutionConverter
    implements AttributeConverter<Set<ReferenceSolutionNode>, String> {

  ObjectMapper mapper = new ObjectMapper();

  @Override
  public String convertToDatabaseColumn(Set<ReferenceSolutionNode> referenceSolution) {
    String value = "";
    mapper.setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());
    try {
      value = mapper.writeValueAsString(referenceSolution);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    return value;
  }

  @Override
  public Set<ReferenceSolutionNode> convertToEntityAttribute(String data) {
    mapper.setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());
    Set<ReferenceSolutionNode> referenceSolution = new HashSet<>();
    try {
      referenceSolution =
          mapper.readValue(data, new TypeReference<Set<ReferenceSolutionNode>>() {});
    } catch (IOException e) {
      throw new IllegalArgumentException(
          "Could not parse reference solution of the training level.");
    }
    return referenceSolution;
  }
}
