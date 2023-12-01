package dev.m0b1.mighty.metrics.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(value = "application")
@Data
public class Properties {

  private PropertiesWebhook webhook;

}
