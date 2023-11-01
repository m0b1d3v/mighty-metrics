package dev.m0b1.mighty.metrics.route;

import dev.m0b1.mighty.metrics.UnitTestBase;
import dev.m0b1.mighty.metrics.auth.AuthAttributes;
import dev.m0b1.mighty.metrics.db.coach.DbCoach;
import dev.m0b1.mighty.metrics.db.coach.DbCoachRepository;
import dev.m0b1.mighty.metrics.db.exercise.DbExercise;
import dev.m0b1.mighty.metrics.db.member.DbMemberRepository;
import dev.m0b1.mighty.metrics.db.score.DbScore;
import dev.m0b1.mighty.metrics.db.score.DbScoreRepository;
import dev.m0b1.mighty.metrics.db.scorecard.DbScoreCard;
import dev.m0b1.mighty.metrics.db.scorecard.DbScoreCardRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RouteCoreUnitTest extends UnitTestBase {

  @InjectMocks
  private RouteCore route;

  @Mock
  private DbCoachRepository dbCoachRepository;

  @Mock
  private DbMemberRepository dbMemberRepository;

  @Mock
  private DbScoreCardRepository dbScoreCardRepository;

  @Mock
  private DbScoreRepository dbScoreRepository;

  @Mock
  private Model model;

  @Mock
  private HttpServletRequest httpServletRequest;

  @Mock
  private OAuth2User oAuth2User;

  @Mock
  private BindingResult bindingResult;

  private final List<DbExercise> originalExerciseList = Collections.nCopies(10, new DbExercise());
  private final List<DbCoach> coaches = new ArrayList<>();
  private final List<DbScore> scores = new ArrayList<>();
  private final DbScoreCard dbScoreCard = new DbScoreCard();
  private final UUID uuid = UUID.randomUUID();

  @BeforeEach
  public void beforeEach() {

    var editableList = new ArrayList<>(originalExerciseList);
    dbScoreCard.setExercises(editableList);

    when(dbCoachRepository.read()).thenReturn(coaches);
    when(dbScoreRepository.read()).thenReturn(scores);
    when(dbScoreCardRepository.upsert(dbScoreCard)).thenReturn(dbScoreCard);
  }

  @Test
  void getCore() {

    var result = route.getCore(model);

    verifyModelAttributes(new DbScoreCard());

    assertEquals("core", result);
  }

  @Test
  void getScoreCard_denied() {

    when(dbMemberRepository.deniedScorecard(oAuth2User, uuid)).thenReturn(true);

    var ex = assertThrows(ResponseStatusException.class, () -> route.getScoreCard(oAuth2User, uuid, model));

    verify(dbScoreCardRepository, never()).read(uuid);

    assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    assertEquals("Scorecard not found.", ex.getReason());
  }

  @Test
  void getScoreCard_allowed() {

    when(dbMemberRepository.deniedScorecard(oAuth2User, uuid)).thenReturn(false);
    when(dbScoreCardRepository.read(uuid)).thenReturn(dbScoreCard);

    var result = route.getScoreCard(oAuth2User, uuid, model);

    verify(dbScoreCardRepository).read(uuid);
    verifyModelAttributes(dbScoreCard);

    assertEquals("core", result);
  }

  @Test
  void postScoreCard_denied() {

    dbScoreCard.setUuid(uuid);

    when(dbMemberRepository.deniedScorecard(oAuth2User, uuid)).thenReturn(true);

    var ex = assertThrows(AccessDeniedException.class, this::postScoreCard);

    assertEquals("Scorecard update denied.", ex.getMessage());
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"10", "11"})
  void postScoreCard_removeExerciseByIndexWithBadInputDoesNotAttemptRemoval(String input) {
    when(httpServletRequest.getParameter("remove")).thenReturn(input);
    postScoreCard();
    assertEquals(originalExerciseList.size(), dbScoreCard.getExercises().size());
  }

  @ParameterizedTest
  @ValueSource(strings = {"0", "1", "9"})
  void postScoreCard_removeExerciseByIndexPossible(String input) {
    when(httpServletRequest.getParameter("remove")).thenReturn(input);
    postScoreCard();
    assertEquals(originalExerciseList.size() - 1, dbScoreCard.getExercises().size());
  }

  @Test
  void postScoreCard_doesNotAddExerciseWithoutParameter() {
    postScoreCard();
    assertEquals(originalExerciseList.size(), dbScoreCard.getExercises().size());
  }

  @Test
  void postScoreCard_addsExerciseWhenGivenParameter() {
    when(httpServletRequest.getParameterMap()).thenReturn(Map.of(
      "add", new String[] { "Literally anything" }
    ));
    postScoreCard();
    assertEquals(originalExerciseList.size() + 1, dbScoreCard.getExercises().size());
  }

  @Test
  void postScoreCard_addsBindingResultErrorsIfFound() {
    when(bindingResult.hasErrors()).thenReturn(true);
    var result = postScoreCard();
    verifyModelAttributes(dbScoreCard);
    verify(dbScoreCardRepository, never()).upsert(dbScoreCard);
    assertEquals("core", result);
  }

  @Test
  void postScoreCard_upsertOnSuccess() {

    dbScoreCard.setUuid(uuid);
    when(oAuth2User.getAttribute(AuthAttributes.ID)).thenReturn("1");

    var result = postScoreCard();
    verify(dbScoreCardRepository).upsert(dbScoreCard);
    assertEquals("redirect:/core/" + uuid, result);
  }

  @Test
  void postScoreCard_redirectsOnSuccess() {

    dbScoreCard.setUuid(uuid);

    var result = postScoreCard();
    verify(dbScoreCardRepository).upsert(dbScoreCard);
    assertEquals("redirect:/core/" + uuid, result);
  }

  private void verifyModelAttributes(DbScoreCard dbScoreCard) {

    verify(dbCoachRepository).read();
    verify(dbScoreRepository).read();

    verify(model).addAttribute("coaches", coaches);
    verify(model).addAttribute("scorecard", dbScoreCard);
    verify(model).addAttribute("scores", scores);
  }

  private String postScoreCard() {
    return route.postScoreCard(oAuth2User, dbScoreCard, bindingResult, model, httpServletRequest);
  }

}
