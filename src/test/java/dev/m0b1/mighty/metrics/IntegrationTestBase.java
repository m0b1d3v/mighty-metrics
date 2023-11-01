package dev.m0b1.mighty.metrics;

import com.deque.html.axecore.playwright.AxeBuilder;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.AriaRole;
import dev.m0b1.mighty.metrics.auth.AuthAttributes;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;

/**
 * One of the most unintuitive pieces of code in this application is setting up our integration tests.
 *
 * MockMvc is set up in the standard Spring way and allows us to inject all sorts of states for testing.
 * Playwright is then added in an interesting (read: dumb) way for page-level assertions and accessibility tests.
 * When HTML is retrieved from MockMvc it can be fed into Playwright as a "data:text/html,<content>" string.
 * Style and script tags can then be added from a randomized port as the root-relative tags won't work.
 *
 * An explanation of the class-level annotations:
 * - @ActiveProfiles("test") allows us to cascade our main/resources/application.yml with application-test.yml
 * - @AutoConfigureMockMvc sets up our mock controller interactions with which we can force authentication states
 * - @SpringBootTest with a random port web environment sets up our application context with a localhost URL
 */
@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IntegrationTestBase extends UnitTestBase {

  /**
   * Random port setup for each integration test cycle from the webEnvironment.
   */
  @LocalServerPort
  private int port;

  /**
   * Controller interaction object for tests.
   */
  @Autowired
  private MockMvc mockMvc;

  /**
   * Browser instance manager.
   */
  private static Playwright playwright;

  /**
   * Emulation of a web browser.
   */
  private static Browser browser;

  /**
   * Browser session manager.
   */
  private BrowserContext browserContext;

  /**
   * Accessibility analysis builder.
   */
  private AxeBuilder axeBuilder;

  /**
   * Emulation of a single browser tab.
   */
  protected Page page;

  /**
   * Launch a Firefox browser before any test starts.
   */
  @BeforeAll
  public static void beforeAll() {
    playwright = Playwright.create();
    browser = playwright.firefox().launch();
  }

  /**
   * Launch a new browser tab and prepare the accessibility analyzer before each test starts.
   */
  @BeforeEach
  public void beforeEach() throws Exception {
    browserContext = browser.newContext();
    page = browserContext.newPage();
    axeBuilder = new AxeBuilder(page);
  }

  /**
   * Close the browser tab after each test ends.
   */
  @AfterEach
  public void afterEach() {
    browserContext.close();
  }

  /**
   * Close the browser manager which in turn closes the browser instance after all tests.
   */
  @AfterAll
  public static void afterAll() {
    playwright.close();
  }

  /**
   * Create the default OAuth2 user authentication to pass to MockMvc requests.
   */
  protected SecurityMockMvcRequestPostProcessors.OAuth2LoginRequestPostProcessor createAuthentication() {
    return createAuthentication("test");
  }

  /**
   * How to create an OAuth2 user authentication to pass to MockMvc requests.
   */
  protected SecurityMockMvcRequestPostProcessors.OAuth2LoginRequestPostProcessor createAuthentication(String prefix) {

    var attributes = Map.of(
      AuthAttributes.GLOBAL_NAME, prefix + "User",
      AuthAttributes.ID, prefix + "Id",
      AuthAttributes.AVATAR, prefix + "Avatar"
    );

    return oauth2Login()
      .authorities(Collections.emptyList())
      .attributes(a -> a.putAll(attributes));
  }

  protected MvcResult loadPage(RequestBuilder requestBuilder) throws Exception {
    return mockMvc.perform(requestBuilder).andReturn();
  }

  protected void assertLoginRedirect(MvcResult mvcResult) {

    var response = mvcResult.getResponse();

    assertStatus(mvcResult, HttpStatus.FOUND.value());
    assertEquals("http://localhost/oauth2/authorization/discord", response.getHeader("Location"));
  }

  protected void assertDenied(MvcResult mvcResult) {
    assertStatus(mvcResult, HttpStatus.FORBIDDEN.value());
  }

  protected void assertStatus(MvcResult mvcResult, int expectedStatus) {

    var response = mvcResult.getResponse();
    var status = response.getStatus();

    assertEquals(expectedStatus, status);
  }

  protected void assertViewName(MvcResult mvcResult, String expectedName) {

    var modelAndView = mvcResult.getModelAndView();

    assertNotNull(modelAndView);
    assertEquals(expectedName, modelAndView.getViewName());
  }

  protected Map<String, Object> assertModel(MvcResult mvcResult) {
    var modelAndView = mvcResult.getModelAndView();
    assertNotNull(modelAndView);
    return modelAndView.getModel();
  }

  /**
   * Feed in HTML from a MockMvc request result for Playwright to render with CSS.
   */
  protected void playwright(MvcResult mvcResult) throws UnsupportedEncodingException {

    var response = mvcResult.getResponse();
    var html = response.getContentAsString();
    var formattedHtml = String.format("data:text/html,%s", html);

    page.navigate(formattedHtml);

    addCss();
  }

  protected Locator getByRole(AriaRole role, String name) {
    var options = new Page.GetByRoleOptions().setName(name);
    return page.getByRole(role, options);
  }

  protected void assertAccessibility() {
    var analysis = axeBuilder.analyze();
    assertTrue(analysis.getViolations().isEmpty());
  }

  private void addCss() {

    var cssPath = String.format("http://localhost:%d/mighty-metrics.css", port);

    var tagOptions = new Page.AddStyleTagOptions();
    tagOptions.setUrl(cssPath);

    page.addStyleTag(tagOptions);
  }

}
