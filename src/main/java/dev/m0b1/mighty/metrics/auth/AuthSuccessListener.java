package dev.m0b1.mighty.metrics.auth;

import dev.m0b1.mighty.metrics.db.member.DbMemberRepository;
import dev.m0b1.mighty.metrics.logging.LogData;
import dev.m0b1.mighty.metrics.logging.ServiceLog;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.slf4j.event.Level;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Upsert a user's information into the database on any successful authentication.
 */
@Component
@RequiredArgsConstructor
public class AuthSuccessListener implements ApplicationListener<AuthenticationSuccessEvent> {

  private final DbMemberRepository dbMemberRepository;
  private final ServiceLog serviceLog;

  @Override
  public void onApplicationEvent(@Nonnull AuthenticationSuccessEvent event) {

    var authentication = event.getAuthentication();

    if (authentication instanceof OAuth2LoginAuthenticationToken token) {

      var oAuth2User = token.getPrincipal();

      serviceLog.run(LogData.builder()
        .level(Level.INFO)
        .message("Login")
        .markers(Map.of("user", oAuth2User.getName()))
      );

      dbMemberRepository.upsert(oAuth2User);
    }
  }
}
