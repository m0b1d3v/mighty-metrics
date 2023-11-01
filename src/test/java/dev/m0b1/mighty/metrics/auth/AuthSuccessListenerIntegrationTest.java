package dev.m0b1.mighty.metrics.auth;

import dev.m0b1.mighty.metrics.IntegrationTestBase;
import dev.m0b1.mighty.metrics.db.member.DbMemberRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.verification.VerificationMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthSuccessListenerIntegrationTest extends IntegrationTestBase {

  @Autowired
  private ApplicationEventPublisher applicationEventPublisher;

  @SpyBean
  private DbMemberRepository dbMemberRepository;

  @Mock
  private AuthenticationSuccessEvent authenticationSuccessEvent;

  @Mock
  private AnonymousAuthenticationToken anonymousAuthenticationToken;

  @Mock
  private OAuth2LoginAuthenticationToken oAuth2LoginAuthenticationToken;

  @Spy
  private OAuth2User oAuth2User;

  @Test
  void notOauth() {

    when(authenticationSuccessEvent.getAuthentication()).thenReturn(anonymousAuthenticationToken);

    verifyUpsert(anonymousAuthenticationToken, never());
  }

  @DirtiesContext
  @Test
  void oauth() {

    oAuth2User.getAttributes().putAll(Map.of(
      AuthAttributes.ID, "testId",
      AuthAttributes.GLOBAL_NAME, "testUser"
    ));

    when(oAuth2LoginAuthenticationToken.getPrincipal()).thenReturn(oAuth2User);

    verifyUpsert(oAuth2LoginAuthenticationToken, times(1));
  }

  private void verifyUpsert(Authentication authentication, VerificationMode verificationMode) {

    when(authenticationSuccessEvent.getAuthentication()).thenReturn(authentication);

    applicationEventPublisher.publishEvent(authenticationSuccessEvent);

    verify(dbMemberRepository, verificationMode).upsert(any());
  }

}
