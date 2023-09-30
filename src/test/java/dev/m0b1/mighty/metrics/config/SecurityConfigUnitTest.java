package dev.m0b1.mighty.metrics.config;

import dev.m0b1.mighty.metrics.UnitTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SecurityConfigUnitTest extends UnitTestBase {

  @InjectMocks
  private SecurityConfig config;

  @Mock
  private HttpSecurity httpSecurity;

  @BeforeEach
  public void beforeEach() throws Exception {
    when(httpSecurity.authorizeHttpRequests(any())).thenReturn(httpSecurity);
    when(httpSecurity.csrf(any())).thenReturn(httpSecurity);
    when(httpSecurity.logout(any())).thenReturn(httpSecurity);
    when(httpSecurity.oauth2Login(any())).thenReturn(httpSecurity);
  }

  @Test
  void filterChainBuilt() throws Exception {

    // Most of this will require integration tests to properly check

    config.filterChain(httpSecurity);

    verify(httpSecurity).authorizeHttpRequests(any());
    verify(httpSecurity).csrf(any());
    verify(httpSecurity).logout(any());
    verify(httpSecurity).oauth2Login(any());
  }

}
