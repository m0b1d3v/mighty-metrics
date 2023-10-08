package dev.m0b1.mighty.metrics.controllers;

import dev.m0b1.mighty.metrics.dao.DaoEnums;
import dev.m0b1.mighty.metrics.dao.DaoMember;
import dev.m0b1.mighty.metrics.dao.DaoScoreCard;
import dev.m0b1.mighty.metrics.models.Exercise;
import dev.m0b1.mighty.metrics.models.OAuth2Attributes;
import dev.m0b1.mighty.metrics.models.ScoreCard;
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
public class CoreController {

  private static final String PATH = "/core";

  private final DaoEnums daoEnums;
  private final DaoMember daoMember;
  private final DaoScoreCard daoScoreCard;

  @GetMapping(PATH)
  public String getCore(Model model) {
    var scoreCard = new ScoreCard();
    addModelAttributes(scoreCard, model);
    return "core";
  }

  @GetMapping(PATH + "/{uuid}")
  public String getScoreCard(
    @AuthenticationPrincipal OAuth2User user,
    @PathVariable UUID uuid,
    Model model
  ) {

    if (daoMember.deniedScorecard(user, uuid)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Scorecard not found.");
    }

    var scoreCard = daoScoreCard.read(uuid);
    addModelAttributes(scoreCard, model);
    return "core";
  }

  @PostMapping(PATH)
  public String postScoreCard(
    @AuthenticationPrincipal OAuth2User user,
    @Valid @ModelAttribute("scorecard") ScoreCard scoreCard,
    BindingResult bindingResult,
    Model model,
    HttpServletRequest httpServletRequest
  ) {

    var uuid = scoreCard.getUuid();
    if (uuid != null && daoMember.deniedScorecard(user, uuid)) {
      throw new AccessDeniedException("Scorecard update denied.");
    }

    var idAttribute = user.getAttribute(OAuth2Attributes.ID);
    Long idMember = null;
    if (idAttribute != null) {
      idMember = Long.valueOf((String) idAttribute);
    }

    scoreCard.setIdMember(idMember);

    removeExerciseByIndexIfGiven(scoreCard, httpServletRequest);
    addExerciseIfDesired(scoreCard, httpServletRequest);

    var result = "core";
    if (bindingResult.hasErrors()) {
      addModelAttributes(scoreCard, model);
    } else {
      scoreCard = daoScoreCard.upsert(scoreCard);
      result = String.format("redirect:%s/%s", PATH, scoreCard.getUuid());
    }

    return result;
  }

  private void addModelAttributes(ScoreCard scoreCard, Model model) {
    model.addAttribute("coaches", daoEnums.coaches());
    model.addAttribute("scorecard", scoreCard);
    model.addAttribute("scores", daoEnums.scores());
  }

  private void addExerciseIfDesired(ScoreCard scoreCard, HttpServletRequest httpServletRequest) {
    if (httpServletRequest.getParameterMap().containsKey("add")) {
      scoreCard.getExercises().add(new Exercise());
    }
  }

  private void removeExerciseByIndexIfGiven(ScoreCard scoreCard, HttpServletRequest httpServletRequest) {

    Integer index = null;

    try {
      var parameter = httpServletRequest.getParameter("remove");
      index = Integer.valueOf(parameter);
    } catch (NumberFormatException ignored) {
      // Ignored
    }

    var exercises = scoreCard.getExercises();
    if (index != null && exercises.size() > index) {
      exercises.remove((int) index);
    }
  }

}
