package dev.m0b1.mighty.metrics.logging;

import dev.m0b1.mighty.metrics.config.Properties;
import dev.m0b1.mighty.metrics.json.JsonUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;

@RequiredArgsConstructor
@Service
@Slf4j
public class ServiceDiscord {

  private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

  private final Properties properties;

  private boolean shouldRun;

  @PostConstruct
  public void postConstruct() {
    shouldRun = properties != null
      && properties.getWebhook() != null
      && StringUtils.isNoneBlank(properties.getWebhook().getAvatar())
      && StringUtils.isNoneBlank(properties.getWebhook().getDestination());
  }

  public void run(Map<String, Object> data) {

    if ( ! shouldRun) {
      return;
    }

    try {

      var content = formatContent(data);
      var body = formatBody(content);
      var request = buildHttpRequest(body);

      HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString());

    } catch (Exception e) {
      log.atError()
        .setMessage("Could not send Discord message")
        .setCause(e)
        .log();
    }
  }

  private String formatContent(Map<String, Object> data) {
    var result = JsonUtil.write(data, "Discord data not recognized");
    result = StringUtils.truncate(result, 1536); // Real limit 2000, lowered for buffer
    result = STR."```json\n\{result}\n```";
    return result;
  }

  private String formatBody(String content) {

    var source = new LinkedHashMap<String, Object>();
    source.put("content", content);
    source.put("username", "Mighty Metrics");
    source.put("avatar_url", properties.getWebhook().getAvatar());

    return JsonUtil.write(source, "Log data not recognized");
  }

  private HttpRequest buildHttpRequest(String body) throws URISyntaxException {

    var uri = new URI(properties.getWebhook().getDestination());

    return HttpRequest.newBuilder()
      .uri(uri)
      .header("Content-Type", "application/json")
      .POST(HttpRequest.BodyPublishers.ofString(body))
      .timeout(Duration.of(10, ChronoUnit.SECONDS))
      .build();
  }

}
