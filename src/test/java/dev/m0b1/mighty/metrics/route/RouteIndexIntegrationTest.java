package dev.m0b1.mighty.metrics.route;

import com.microsoft.playwright.options.AriaRole;
import dev.m0b1.mighty.metrics.IntegrationTestBase;
import dev.m0b1.mighty.metrics.auth.AuthAttributes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.RequestBuilder;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

class RouteIndexIntegrationTest extends IntegrationTestBase {

  private RequestBuilder anonymousRequest;
  private RequestBuilder authenticatedRequest;

  @BeforeEach
  public void beforeEach() throws Exception {

    super.beforeEach();

    var path = "/";
    var authentication = createAuthentication();

    anonymousRequest = get(path);
    authenticatedRequest = get(path).with(authentication);
  }

  @Test
  void anonymous() throws Exception {

    var mvcResult = loadPage(anonymousRequest);

    assertStatus(mvcResult, HttpStatus.OK.value());
    assertViewName(mvcResult, "index");

    var model = assertModel(mvcResult);
    assertFalse((Boolean) model.get(AuthAttributes.LOGGED_IN));
    assertFalse(model.containsKey(AuthAttributes.NAME));
    assertFalse(model.containsKey(AuthAttributes.AVATAR_IMAGE_URL));

    playwright(mvcResult);
    assertAccessibility();
    assertThat(page).hasTitle("Mighty Metrics - Home");
    assertThat(getByRole(AriaRole.LINK, "log in via Discord")).isVisible();
  }

  @Test
  void authenticated() throws Exception {

    var mvcResult = loadPage(authenticatedRequest);

    assertStatus(mvcResult, HttpStatus.OK.value());
    assertViewName(mvcResult, "member");

    var model = assertModel(mvcResult);
    assertTrue((Boolean) model.get(AuthAttributes.LOGGED_IN));
    assertEquals("testUser", model.get(AuthAttributes.NAME));
    assertEquals("https://cdn.discordapp.com/avatars/1/testAvatar.png", model.get(AuthAttributes.AVATAR_IMAGE_URL));

    playwright(mvcResult);
    assertAccessibility();
    assertThat(page).hasTitle("Mighty Metrics - testUser");
    assertThat(getByRole(AriaRole.LINK, "Core")).isVisible();
  }

}
