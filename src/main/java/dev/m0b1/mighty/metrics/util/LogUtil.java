package dev.m0b1.mighty.metrics.util;

import lombok.experimental.UtilityClass;
import net.logstash.logback.marker.LogstashMarker;

import static net.logstash.logback.marker.Markers.append;

@UtilityClass
public final class LogUtil {

  public static LogstashMarker kv(String key, Object value) {
    return append(key, value);
  }

}
