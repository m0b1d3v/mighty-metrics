package dev.m0b1.mighty.metrics.controllers;

import dev.m0b1.mighty.metrics.UnitTestBase;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IndexControllerUnitTest extends UnitTestBase {

  @InjectMocks
  private IndexController controller;

  @Test
  void view() {
    var result = controller.index();
    assertEquals("index", result);
  }

}
