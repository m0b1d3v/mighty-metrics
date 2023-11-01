package dev.m0b1.mighty.metrics.auth;

import dev.m0b1.mighty.metrics.UnitTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.ui.Model;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthControllerAdviceUnitTest extends UnitTestBase {

  @InjectMocks
  private AuthControllerAdvice advice;

  @Mock
  private Model model;

  @Mock
  private OAuth2User user;

  @BeforeEach
  public void beforeEach() {
    when(user.getAttribute("global_name")).thenReturn("1");
    when(user.getAttribute("id")).thenReturn("2");
    when(user.getAttribute("avatar")).thenReturn("3");
  }

  @Test
  void noUserStateIndicatesNotLoggedIn() {

    advice.addGlobalAttributes(model, null);

    verify(model).addAttribute("loggedIn", false);
    verify(model, never()).addAttribute(eq("name"), anyString());
    verify(model, never()).addAttribute(eq("avatarImageUrl"), anyString());
  }

  @Test
  void userStateIndicatesLoggedIn() {

    advice.addGlobalAttributes(model, user);

    verify(model).addAttribute("loggedIn", true);
    verify(model).addAttribute(eq("name"), anyString());
    verify(model).addAttribute(eq("avatarImageUrl"), anyString());
  }

  @Test
  void userStateSetsNameAndAvatarImageUrl() {

    advice.addGlobalAttributes(model, user);

    verify(model).addAttribute("name", "1");
    verify(model).addAttribute("avatarImageUrl", "https://cdn.discordapp.com/avatars/2/3.png");
  }

}
