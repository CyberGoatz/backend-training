package cz.cyberrange.platform.training.api.converters;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * Deserializes UTC time with 'Z' suffix from Angular typescript Date, e.g., the date: '2018-11-30T10:26:02.727Z'
 *
 */
public class LocalDateTimeUTCDeserializer extends StdDeserializer<LocalDateTime> {

    public LocalDateTimeUTCDeserializer() {
        super(LocalDateTime.class);
    }

    @Override
    public LocalDateTime deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        String value = jp.readValueAs(String.class);
        if (value == null || value.isBlank()) {
            return null;
        }
        Instant instant = Instant.parse(value);
        return LocalDateTime.ofInstant(instant, ZoneId.of(ZoneOffset.UTC.getId()));
    }

}
