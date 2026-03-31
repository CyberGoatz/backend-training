package cz.cyberrange.platform.training.api.converters;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.time.LocalTime;

public class LocalTimeDeserializer extends StdDeserializer<LocalTime> {

  public LocalTimeDeserializer() {
    super(LocalTime.class);
  }

  @Override
  public LocalTime deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
    return LocalTime.parse(jp.readValueAs(String.class));
  }
}
