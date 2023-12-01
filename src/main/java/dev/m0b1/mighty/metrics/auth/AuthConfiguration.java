package dev.m0b1.mighty.metrics.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

/**
 * Setup Spring to conditionally protect our application.
 */
@Configuration
public class AuthConfiguration {

  private static final String[] GUEST_ALLOWED_PATTERNS = new String[]{
    "/",
    "/error/**",
    "/webjars/**",
    "/*.png",
    "/*.ico",
    "/*.css",
    "/*.js"
  };

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
    return httpSecurity
      .authorizeHttpRequests(a -> a
        .requestMatchers(GUEST_ALLOWED_PATTERNS).permitAll()
        .anyRequest().authenticated()
      )
      .csrf(c -> c.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
      .logout(l -> l.logoutSuccessUrl("/").permitAll())
      .oauth2Login(o -> o.loginPage("/"))
      .build();
  }

}
