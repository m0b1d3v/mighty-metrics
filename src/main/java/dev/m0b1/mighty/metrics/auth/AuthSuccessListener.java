package dev.m0b1.mighty.metrics.auth;

import dev.m0b1.mighty.metrics.db.member.DbMemberRepository;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * Upsert a user's information into the database on any successful authentication.
 */
@Component
@RequiredArgsConstructor
public class AuthSuccessListener implements ApplicationListener<AuthenticationSuccessEvent> {

  private final DbMemberRepository dbMemberRepository;

  @Override
  public void onApplicationEvent(@Nonnull AuthenticationSuccessEvent event) {

    var authentication = event.getAuthentication();

    if (authentication instanceof OAuth2LoginAuthenticationToken token) {
      var oAuth2User = token.getPrincipal();
      dbMemberRepository.upsert(oAuth2User);
    }
  }
}
