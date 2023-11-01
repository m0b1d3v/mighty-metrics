package dev.m0b1.mighty.metrics.route;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RouteIndex {

  private static final String PATH = "/";

  @GetMapping(PATH)
  public String index(@AuthenticationPrincipal OAuth2User user) {

    var view = "index";

    if (user != null) {
      view = "member";
    }

    return view;
  }

}
