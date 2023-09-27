package dev.m0b1.mighty.metrics.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
public class SecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {

    var guestAllowedPatterns = new String[]{
      "/",
      "/error/**",
      "/*.png",
      "/*.ico",
      "/*.css"
    };

    return httpSecurity
      .authorizeHttpRequests(a -> a
        .requestMatchers(guestAllowedPatterns).permitAll()
        .anyRequest().authenticated()
      )
      .csrf(c -> c.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
      .logout(l -> l.logoutSuccessUrl("/").permitAll())
      .oauth2Login(Customizer.withDefaults())
      .build();
  }

}
