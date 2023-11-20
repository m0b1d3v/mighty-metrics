package dev.m0b1.mighty.metrics.route;

import dev.m0b1.mighty.metrics.auth.AuthUtil;
import dev.m0b1.mighty.metrics.db.coach.DbCoachRepository;
import dev.m0b1.mighty.metrics.db.scorecard.DbScoreCardExercise;
import dev.m0b1.mighty.metrics.db.member.DbMemberRepository;
import dev.m0b1.mighty.metrics.db.score.DbScoreRepository;
import dev.m0b1.mighty.metrics.db.scorecard.DbScoreCard;
import dev.m0b1.mighty.metrics.db.scorecard.DbScoreCardRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class RouteCore {

  public static final String PATH = "/core";

  private final DbCoachRepository dbCoachRepository;
  private final DbMemberRepository dbMemberRepository;
  private final DbScoreCardRepository dbScoreCardRepository;
  private final DbScoreRepository dbScoreRepository;

  @GetMapping(PATH)
  public String getCore(Model model) {
    var dbScoreCard = new DbScoreCard();
    dbScoreCard.getExercises().add(new DbScoreCardExercise());
    addModelAttributes(dbScoreCard, model);
    return "core";
  }

  @GetMapping(PATH + "/{uuid}")
  public String getScoreCard(
    @AuthenticationPrincipal OAuth2User user,
    @PathVariable UUID uuid,
    Model model
  ) {

    if (dbMemberRepository.deniedScorecard(user, uuid)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Scorecard not found.");
    }

    var dbScoreCard = dbScoreCardRepository.read(uuid);
    addModelAttributes(dbScoreCard, model);
    return "core";
  }

  @PostMapping(PATH)
  public String postScoreCard(
    @AuthenticationPrincipal OAuth2User user,
    @Valid @ModelAttribute("scorecard") DbScoreCard dbScoreCard,
    BindingResult bindingResult,
    Model model,
    HttpServletRequest httpServletRequest
  ) {

    throwIfDeniedScorecard(dbScoreCard, user);

    removeExerciseByIndexIfGiven(dbScoreCard, httpServletRequest);
    addExerciseIfDesired(dbScoreCard, httpServletRequest);

    var idUser = AuthUtil.getUserIdIfAttributePresent(user);
    dbScoreCard.setIdMember(idUser);

    var result = PATH;
    if (bindingResult.hasErrors()) {
      addModelAttributes(dbScoreCard, model);
    } else if (shouldDeleteScorecard(httpServletRequest)) {
      dbScoreCardRepository.delete(dbScoreCard);
      result = String.format("redirect:%s", PATH);
    } else {
      dbScoreCard = dbScoreCardRepository.upsert(dbScoreCard);
      result = String.format("redirect:%s/%s", PATH, dbScoreCard.getUuid());
    }

    return result;
  }


  private void throwIfDeniedScorecard(DbScoreCard dbScoreCard, OAuth2User user) {
    var uuid = dbScoreCard.getUuid();
    if (uuid != null && dbMemberRepository.deniedScorecard(user, uuid)) {
      throw new AccessDeniedException("Scorecard update denied.");
    }
  }

  private boolean shouldDeleteScorecard(HttpServletRequest httpServletRequest) {
    return httpServletRequest.getParameterMap().containsKey("delete");
  }

  private void addExerciseIfDesired(DbScoreCard dbScoreCard, HttpServletRequest httpServletRequest) {
    if (httpServletRequest.getParameterMap().containsKey("add")) {
      dbScoreCard.getExercises().add(new DbScoreCardExercise());
    }
  }

  private void removeExerciseByIndexIfGiven(DbScoreCard dbScoreCard, HttpServletRequest httpServletRequest) {

    Integer index = null;

    try {
      var parameter = httpServletRequest.getParameter("remove");
      index = Integer.valueOf(parameter);
    } catch (NumberFormatException ignored) {
      // Ignored
    }

    var exercises = dbScoreCard.getExercises();
    if (index != null && exercises.size() > index) {
      exercises.remove((int) index);
    }
  }

  private void addModelAttributes(DbScoreCard dbScoreCard, Model model) {
    model.addAttribute("coaches", dbCoachRepository.read());
    model.addAttribute("scorecard", dbScoreCard);
    model.addAttribute("scores", dbScoreRepository.read());
  }

}