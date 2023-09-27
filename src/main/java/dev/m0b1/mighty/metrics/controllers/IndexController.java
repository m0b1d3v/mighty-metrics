package dev.m0b1.mighty.metrics.controllers;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

  @GetMapping("/")
  public String index(@AuthenticationPrincipal OAuth2User user, Model model) {

    var loggedIn = user != null;
    model.addAttribute("loggedIn", loggedIn);

    if (loggedIn) {
      model.addAttribute("name", user.getAttribute("global_name"));
      model.addAttribute("avatarImageUrl", buildAvatarImageUrl(user));
    }

    return "index";
  }

  /**
   * Given an OAuth users information, build an image URL that should resolve to their avatar.
   *
   * @see <a href="https://discord.com/developers/docs/reference#image-formatting">URL formatting</a>
   */
  private String buildAvatarImageUrl(OAuth2User user) {

    var userId = user.getAttribute("id");
    var avatar = user.getAttribute("avatar");

    return String.format("https://cdn.discordapp.com/avatars/%s/%s.png", userId, avatar);
  }

}
