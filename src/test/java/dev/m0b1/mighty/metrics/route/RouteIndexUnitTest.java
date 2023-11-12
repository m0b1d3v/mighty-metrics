package dev.m0b1.mighty.metrics.route;

import dev.m0b1.mighty.metrics.UnitTestBase;
import dev.m0b1.mighty.metrics.db.score.DbScoreRepository;
import dev.m0b1.mighty.metrics.db.scorecard.DbScoreCardRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RouteIndexUnitTest extends UnitTestBase {

  @InjectMocks
  private RouteIndex route;

  @Mock
  private OAuth2User user;

  @Mock
  private DbScoreRepository dbScoreRepository;

  @Mock
  private DbScoreCardRepository dbScoreCardRepository;

  @Mock
  private Model model;

  @Test
  void noUserStateShowsIndexView() {
    var result = route.index(null, model);
    assertEquals("index", result);
  }

  @Test
  void userStateShowsMemberView() {
    var result = route.index(user, model);
    assertEquals("member", result);
  }

}
