package dev.m0b1.mighty.metrics.auth;

import dev.m0b1.mighty.metrics.UnitTestBase;
import dev.m0b1.mighty.metrics.db.member.DbMemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthSuccessListenerUnitTest extends UnitTestBase {

  @InjectMocks
  private AuthSuccessListener listener;

  @Mock
  private DbMemberRepository dbMemberRepository;

  @Mock
  private AuthenticationSuccessEvent event;

  public static Stream<Arguments> onApplicationEvent_nonOauthIgnored() {
    return Stream.of(
      null,
      Arguments.of(mock(TestingAuthenticationToken.class))
    );
  }

  @ParameterizedTest
  @MethodSource
  void onApplicationEvent_nonOauthIgnored(Authentication authentication) {
    when(event.getAuthentication()).thenReturn(authentication);
    listener.onApplicationEvent(event);
    verify(dbMemberRepository, never()).upsert(any());
  }

  @Test
  void onApplicationEvent_oauthEvent() {

    var token = mock(OAuth2LoginAuthenticationToken.class);
    var oAuth2User = mock(OAuth2User.class);

    when(token.getPrincipal()).thenReturn(oAuth2User);
    when(event.getAuthentication()).thenReturn(token);

    listener.onApplicationEvent(event);

    verify(dbMemberRepository).upsert(oAuth2User);
  }

}
