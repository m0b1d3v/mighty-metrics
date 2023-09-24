package dev.m0b1.mighty.metrics.util;

import io.sentry.Sentry;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public final class ErrorMonitor {

  private static final String SENTRY_DSN = System.getenv("SENTRY_DSN");
  private static final String START_INDICATION_MESSAGE = "Error monitoring started.";

  /**
   * Starts error/exception monitoring via Sentry if "SENTRY_DSN" environment variable is set.
   */
  public static void start() {

    if (canNotStart()) {
      return;
    }

    initialize();
    indicateStart();
  }

  private static boolean canNotStart() {
    return SENTRY_DSN == null || SENTRY_DSN.isEmpty();
  }

  private static void initialize() {
    Sentry.init(options -> options.setDsn(SENTRY_DSN));
  }

  private static void indicateStart() {
    log.info(START_INDICATION_MESSAGE);
    Sentry.captureMessage(START_INDICATION_MESSAGE);
  }

}
