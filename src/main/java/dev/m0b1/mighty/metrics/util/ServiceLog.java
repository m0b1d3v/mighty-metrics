package dev.m0b1.mighty.metrics.util;

import dev.m0b1.mighty.metrics.config.Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;

import static net.logstash.logback.marker.Markers.append;

@RequiredArgsConstructor
@Service
@Slf4j
public class ServiceLog {

  private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

  private final Properties properties;
  private final ServiceJson serviceJson;

  public void run(Level level, String message) {
    run(level, message, null);
  }

  public void run(Level level, String message, Map<String, Object> markers) {
    run(level, message, null, markers);
  }

  public void run(Level level, String message, Throwable throwable, Map<String, Object> markers) {

    var discordData = new LinkedHashMap<String, Object>();

    var logEvent = log.atLevel(level);
    discordData.put("level", level.toString());

    if (StringUtils.isNotEmpty(message)) {
      logEvent = logEvent.setMessage(message);
      discordData.put("message", message);
    }

    if (throwable != null) {
      logEvent = logEvent.setCause(throwable);
      discordData.put("cause", throwable.toString());
    }

    if (MapUtils.isNotEmpty(markers)) {
      for (var entry : markers.entrySet()) {
        var k = entry.getKey();
        var v = entry.getValue();
        logEvent = logEvent.addMarker(append(k, v));
        discordData.put(k, v);
      }
    }

    logEvent.log();

    sendDiscordMessage(discordData);
  }

  private void sendDiscordMessage(Map<String, Object> data) {

    if (properties == null || properties.getWebhook() == null) {
      return;
    }

    var webhook = properties.getWebhook();
    var avatar = webhook.getAvatar();
    var destination = webhook.getDestination();

    if (StringUtils.isAnyBlank(avatar, destination)) {
      return;
    }

    try {

      var content = serviceJson.write(data, "Discord data not recognized");
      content = StringUtils.truncate(content, 1536); // Real limit 2000, lowered for buffer
      content = STR."```json\n\{content}\n```";

      var discordJson = new LinkedHashMap<String, Object>();
      discordJson.put("content", content);
      discordJson.put("username", "Mighty Metrics");
      discordJson.put("avatar_url", avatar);

      var uri = new URI(destination);
      var body = serviceJson.write(discordJson, "Log data not recognized");

      var request = HttpRequest.newBuilder()
        .uri(uri)
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(body))
        .timeout(Duration.of(10, ChronoUnit.SECONDS))
        .build();

      HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString());

    } catch (Exception e) {
      log.atError()
        .setMessage("Could not send Discord message")
        .setCause(e)
        .log();
    }
  }

}
