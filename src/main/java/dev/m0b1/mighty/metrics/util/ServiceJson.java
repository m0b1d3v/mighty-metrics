package dev.m0b1.mighty.metrics.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;
import org.springframework.stereotype.Service;

import java.util.Map;

@RequiredArgsConstructor
@Service
@Slf4j
public class ServiceJson {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
    .registerModule(new JavaTimeModule());

  private final ServiceLog serviceLog;

  public <T> T read(String json, TypeReference<T> typeReference) {

    T result = null;

    try {
      if (json != null) {
        result = OBJECT_MAPPER.readValue(json, typeReference);
      }
    } catch (Exception e) {
      serviceLog.run(Level.ERROR, "Exception encountered with reading JSON to object", e, Map.of(
        "typeReference", typeReference,
        "json", json
      ));
    }

    return result;
  }

  public String write(Object input, String defaultValue) {

    var result = defaultValue;

    try {
      result = OBJECT_MAPPER.writeValueAsString(input);
    } catch (Exception e) {
      serviceLog.run(Level.ERROR, "Exception encountered with writing object to JSON", e, null);
    }

    return result;
  }

}
