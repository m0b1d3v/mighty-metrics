package dev.m0b1.mighty.metrics.auth;

import lombok.experimental.UtilityClass;
import org.springframework.security.oauth2.core.user.OAuth2User;

@UtilityClass
public final class AuthUtil {

  public Long getUserIdIfAttributePresent(OAuth2User user) {

    Long result = null;

    var id = user.getAttribute(AuthAttributes.ID);
    if (id != null) {
      result = Long.valueOf((String) id);
    }

    return result;
  }

}
