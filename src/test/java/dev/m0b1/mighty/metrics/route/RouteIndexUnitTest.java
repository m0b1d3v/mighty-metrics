package dev.m0b1.mighty.metrics.route;

import dev.m0b1.mighty.metrics.UnitTestBase;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.oauth2.core.user.OAuth2User;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RouteIndexUnitTest extends UnitTestBase {

  @InjectMocks
  private RouteIndex route;

  @Mock
  private OAuth2User user;

  @Test
  void noUserStateShowsIndexView() {
    var result = route.index(null);
    assertEquals("index", result);
  }

  @Test
  void userStateShowsMemberView() {
    var result = route.index(user);
    assertEquals("member", result);
  }

}
