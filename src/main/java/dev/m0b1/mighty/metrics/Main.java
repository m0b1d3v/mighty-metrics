package dev.m0b1.mighty.metrics;

import dev.m0b1.mighty.metrics.util.ErrorMonitor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {

  public static void main(String[] args) {

    ErrorMonitor.start();

    log.info("Hello, world");
  }

}