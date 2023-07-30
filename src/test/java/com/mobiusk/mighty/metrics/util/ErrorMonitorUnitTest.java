package com.mobiusk.mighty.metrics.util;

import com.mobiusk.mighty.metrics.TestBase;
import org.junit.jupiter.api.Test;

class ErrorMonitorUnitTest extends TestBase {

  @Test
  void utilityClass() throws NoSuchMethodException {
    assertUtilityClass(ErrorMonitor.class);
  }

  // It would be very nice to have more tests for this, but I'm not willing to mock external statics

}
