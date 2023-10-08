package dev.m0b1.mighty.metrics.services;

import dev.m0b1.mighty.metrics.dao.DaoMember;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthenticationSuccessListener implements ApplicationListener<AuthenticationSuccessEvent> {

  private final DaoMember daoMember;

  @Override
  public void onApplicationEvent(AuthenticationSuccessEvent event) {

    var authentication = event.getAuthentication();

    if (authentication instanceof OAuth2LoginAuthenticationToken token) {
      var oAuth2User = token.getPrincipal();
      daoMember.upsert(oAuth2User);
    }
  }
}
