package dev.m0b1.mighty.metrics.auth;

import dev.m0b1.mighty.metrics.IntegrationTestBase;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

class AuthConfigurationIntegrationTest extends IntegrationTestBase {

  @ParameterizedTest
  @ValueSource(strings = {
    "/apple-touch-icon.png",
    "/favicon.ico",
    "/favicon-16x16.png",
    "/favicon-32x32.png",
    "/logo.png",
    "/mighty-metrics.css"
  })
  void staticAssetsAccess(String path) throws Exception {

    var request = get(path);

    var mvcResult = loadPage(request);

    assertStatus(mvcResult, HttpStatus.OK.value());
  }

}
