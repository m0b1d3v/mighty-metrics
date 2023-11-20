package dev.m0b1.mighty.metrics.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public final class JsonUtil {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  public static <T> T read(String json, TypeReference<T> typeReference) {

    T result = null;

    try {
      if (json != null) {
        result = OBJECT_MAPPER.readValue(json, typeReference);
      }
    } catch (Exception e) {
      log.atError()
        .setMessage("Exception encountered with reading JSON to object")
        .setCause(e)
        .addMarker(LogUtil.kv("typeReference", typeReference))
        .addMarker(LogUtil.kv("json", json))
        .log();
    }

    return result;
  }

  public static String write(Object input, String defaultValue) {

    var result = defaultValue;

    try {
      result = OBJECT_MAPPER.writeValueAsString(input);
    } catch (Exception e) {
      log.atError()
        .setMessage("Exception encountered with writing object to JSON")
        .setCause(e)
        .addMarker(LogUtil.kv("json", input))
        .log();
    }

    return result;
  }

}
