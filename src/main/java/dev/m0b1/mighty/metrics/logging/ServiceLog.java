package dev.m0b1.mighty.metrics.logging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;

import static net.logstash.logback.marker.Markers.append;

@RequiredArgsConstructor
@Service
@Slf4j
public class ServiceLog {

  private final ServiceDiscord serviceDiscord;

  public void run(LogData.LogDataBuilder logData) {
    run(logData.build());
  }

  public void run(LogData logData) {

    var discordData = new LinkedHashMap<String, Object>();

    var level = logData.getLevel();
    var logEvent = log.atLevel(level);
    discordData.put("level", level.toString());

    var message = logData.getMessage();
    if (StringUtils.isNotEmpty(message)) {
      logEvent = logEvent.setMessage(message);
      discordData.put("message", message);
    }

    var throwable = logData.getThrowable();
    if (throwable != null) {
      logEvent = logEvent.setCause(throwable);
      discordData.put("cause", throwable.toString());
    }

    var markers = logData.getMarkers();
    if (MapUtils.isNotEmpty(markers)) {
      for (var entry : markers.entrySet()) {
        var k = entry.getKey();
        var v = entry.getValue();
        logEvent = logEvent.addMarker(append(k, v));
        discordData.put(k, v);
      }
    }

    logEvent.log();

    serviceDiscord.run(discordData);
  }

}
