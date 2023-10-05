package dev.m0b1.mighty.metrics.controllers;

import dev.m0b1.mighty.metrics.UnitTestBase;
import dev.m0b1.mighty.metrics.enums.EnumCoach;
import dev.m0b1.mighty.metrics.enums.EnumScore;
import dev.m0b1.mighty.metrics.models.Exercise;
import dev.m0b1.mighty.metrics.models.ScoreCard;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CoreControllerUnitTest extends UnitTestBase {

  @InjectMocks
  private CoreController controller;

  @Mock
  private Model model;

  @Mock
  private HttpServletRequest httpServletRequest;

  private List<Exercise> originalExerciseList;
  private final ScoreCard scoreCard = new ScoreCard();

  @BeforeEach
  public void beforeEach() {
    originalExerciseList = Collections.nCopies(10, new Exercise());
    var editableList = new ArrayList<>(originalExerciseList);
    scoreCard.setExercises(editableList);
  }

  @Test
  void getCore_view() {
    assertEquals("core", controller.getCore(model));
  }

  @Test
  void getCore_addsModelAttributes() {
    controller.getCore(model);
    verifyModelAttributes(new ScoreCard());
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"10", "11"})
  void postCore_removeExerciseByIndexWithBadInputDoesNotAttemptRemoval(String input) {
    when(httpServletRequest.getParameter("remove")).thenReturn(input);
    controller.postCore(scoreCard, null, model, httpServletRequest);
    assertEquals(originalExerciseList.size(), scoreCard.getExercises().size());
  }

  @ParameterizedTest
  @ValueSource(strings = {"0", "1", "9"})
  void postCore_removeExerciseByIndexPossible(String input) {
    when(httpServletRequest.getParameter("remove")).thenReturn(input);
    controller.postCore(scoreCard, null, model, httpServletRequest);
    assertEquals(originalExerciseList.size() - 1, scoreCard.getExercises().size());
  }

  @Test
  void postCore_doesNotAddExerciseWithoutParameter() {
    controller.postCore(scoreCard, null, model, httpServletRequest);
    assertEquals(originalExerciseList.size(), scoreCard.getExercises().size());
  }

  @Test
  void postCore_addsExerciseWhenGivenParameter() {
    when(httpServletRequest.getParameterMap()).thenReturn(Map.of(
      "add", new String[] { "Literally anything" }
    ));
    controller.postCore(scoreCard, null, model, httpServletRequest);
    assertEquals(originalExerciseList.size() + 1, scoreCard.getExercises().size());
  }

  @Test
  void postCore_addsModelAttributes() {
    controller.postCore(scoreCard, null, model, httpServletRequest);
    verifyModelAttributes(scoreCard);
  }

  private void verifyModelAttributes(ScoreCard scoreCard) {
    verify(model).addAttribute("coaches", EnumCoach.values());
    verify(model).addAttribute("scorecard", scoreCard);
    verify(model).addAttribute("scores", EnumScore.values());
  }

}
