package dev.m0b1.mighty.metrics.controllers;

import dev.m0b1.mighty.metrics.dao.DaoReader;
import dev.m0b1.mighty.metrics.models.Exercise;
import dev.m0b1.mighty.metrics.models.ScoreCard;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class CoreController {

  private final DaoReader daoReader;

  @GetMapping("/core")
  public String getCore(Model model) {
    var scoreCard = new ScoreCard();
    addModelAttributes(scoreCard, model);
    return "core";
  }

  @PostMapping("/core")
  public String postCore(
    @Valid @ModelAttribute("scorecard") ScoreCard scoreCard,
    BindingResult ignored,
    Model model,
    HttpServletRequest httpServletRequest
  ) {

    removeExerciseByIndexIfGiven(scoreCard, httpServletRequest);
    addExerciseIfDesired(scoreCard, httpServletRequest);


    addModelAttributes(scoreCard, model);
    return "core";
  }

  private void addModelAttributes(ScoreCard scoreCard, Model model) {
    model.addAttribute("coaches", daoReader.coaches());
    model.addAttribute("scorecard", scoreCard);
    model.addAttribute("scores", daoReader.scores());
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
