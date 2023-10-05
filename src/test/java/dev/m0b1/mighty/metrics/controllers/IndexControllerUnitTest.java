package dev.m0b1.mighty.metrics.controllers;

import dev.m0b1.mighty.metrics.UnitTestBase;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.oauth2.core.user.OAuth2User;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IndexControllerUnitTest extends UnitTestBase {

  @InjectMocks
  private IndexController controller;

  @Mock
  private OAuth2User user;

  @Test
  void noUserStateShowsIndexView() {
    var result = controller.index(null);
    assertEquals("index", result);
  }

  @Test
  void userStateShowsMemberView() {
    var result = controller.index(user);
    assertEquals("member", result);
  }

}
