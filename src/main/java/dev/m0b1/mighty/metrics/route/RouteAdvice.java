package dev.m0b1.mighty.metrics.route;

import dev.m0b1.mighty.metrics.auth.AuthAttributes;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;

/**
 * Inject current authentication state into each controller's model for access in the view.
 */
@ControllerAdvice
public class RouteAdvice {

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
  public String handle(MaxUploadSizeExceededException e, Model model) {
    return "error/413";
  }

  @ModelAttribute
  public void addGlobalAttributes(Model model, @AuthenticationPrincipal OAuth2User user) {

    var loggedIn = user != null;
    model.addAttribute(AuthAttributes.LOGGED_IN, loggedIn);

    if (loggedIn) {
      model.addAttribute(AuthAttributes.NAME, user.getAttribute(AuthAttributes.GLOBAL_NAME));
      model.addAttribute(AuthAttributes.AVATAR_IMAGE_URL, buildAvatarImageUrl(user));
    }
  }

  /**
   * Given an OAuth users information, build an image URL that should resolve to their avatar.
   *
   * @see <a href="https://discord.com/developers/docs/reference#image-formatting">URL formatting</a>
   */
  private String buildAvatarImageUrl(OAuth2User user) {

    var userId = user.getAttribute(AuthAttributes.ID);
    var avatar = user.getAttribute(AuthAttributes.AVATAR);

    return STR."https://cdn.discordapp.com/avatars/\{userId}/\{avatar}.png";
  }

}
