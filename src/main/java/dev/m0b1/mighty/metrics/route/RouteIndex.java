package dev.m0b1.mighty.metrics.route;

import dev.m0b1.mighty.metrics.auth.AuthUtil;
import dev.m0b1.mighty.metrics.db.score.DbScoreRepository;
import dev.m0b1.mighty.metrics.db.scorecard.DbScoreCard;
import dev.m0b1.mighty.metrics.db.scorecard.DbScoreCardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class RouteIndex {

  private static final String PATH = "/";

  private static final DecimalFormat STATISTIC_FORMAT = new DecimalFormat("#,###");

  private final DbScoreRepository dbScoreRepository;
  private final DbScoreCardRepository dbScoreCardRepository;

  @GetMapping(PATH)
  public String index(
    @AuthenticationPrincipal OAuth2User user,
    Model model
  ) {

    var view = "index";

    if (user != null) {

      var idUser = AuthUtil.getUserIdIfAttributePresent(user);

      var scorecards = dbScoreCardRepository.readAll(idUser);
      model.addAttribute("scorecards", scorecards);

      var statistics = generateStatistics(scorecards);
      model.addAttribute("statistics", statistics);

      var scores = dbScoreRepository.read();
      model.addAttribute("scores", scores);

      view = "member";
    }

    return view;
  }

  private List<String> generateStatistics(List<DbScoreCard> dbScoreCards) {

    var result = new LinkedList<String>();

    if (dbScoreCards == null || dbScoreCards.isEmpty()) {
      result.add("No statistics yet, go do some arm circles!");
    } else {
      statisticTotalWorkouts(result, dbScoreCards);
      statisticScore(result, dbScoreCards);
      statisticWorkoutIntensity(result, dbScoreCards);
      statisticTopRank(result, dbScoreCards);
      statisticGroupRank(result, dbScoreCards);
      statisticMighteriumCollected(result, dbScoreCards);
    }

    return result;
  }

  private void statisticTotalWorkouts(List<String> statistics, List<DbScoreCard> dbScoreCards) {
    var result = dbScoreCards.size();
    if (result > 2) {
      addStatistic(statistics, result, "workout sessions");
    }
  }

  private void statisticScore(List<String> statistics, List<DbScoreCard> dbScoreCards) {

    var scorecardsSize = dbScoreCards.size();
    var scoreSum = 0;

    for (var card : dbScoreCards) {
      if (card.getIdScorePersonal() != null) {
        scoreSum += card.getIdScorePersonal();
      }
    }

    if (scorecardsSize > 1 && scoreSum > 0) {
      var result = scoreSum / scorecardsSize;
      if (result > 5) { // C or better
        addStatistic(statistics, result, "average personal score");
      }
    }
  }

  private void statisticWorkoutIntensity(List<String> statistics, List<DbScoreCard> dbScoreCards) {

    var scorecardsWithIntensity = 0;
    var intensitySum = 0;

    for (var card : dbScoreCards) {
      if (card.getWorkoutIntensity() != null) {
        scorecardsWithIntensity++;
        intensitySum += card.getWorkoutIntensity();
      }
    }

    if (scorecardsWithIntensity > 1 && intensitySum > 0) {
      var result = intensitySum / scorecardsWithIntensity;
      addStatistic(statistics, result, "average workout intensity");
    }
  }

  private void statisticTopRank(List<String> statistics, List<DbScoreCard> dbScoreCards) {

    var result = 0;

    for (var card : dbScoreCards) {
      if (card.getIdScorePersonal() != null && card.getIdScorePersonal() > 3) {
        result++;
      }
    }

    if (result > 0) {
      addStatistic(statistics, result, "S/S+ ranks earned");
    }
  }

  private void statisticGroupRank(List<String> statistics, List<DbScoreCard> dbScoreCards) {

    var result = 0;

    for (var card : dbScoreCards) {
      var scorePersonal = card.getIdScorePersonal();
      var scoreGroup = card.getIdScoreGroup();
      if (scorePersonal != null && scoreGroup != null && scorePersonal >= scoreGroup) {
        result++;
      }
    }

    if (result > 0) {
      addStatistic(statistics, result, "group workout gold stars");
    }
  }

  private void statisticMighteriumCollected(List<String> statistics, List<DbScoreCard> dbScoreCards) {

    var result = 0;

    for (var card : dbScoreCards) {
      if (card.getMighteriumCollected() != null) {
        result += card.getMighteriumCollected();
      }
    }

    if (result > 0) {
      addStatistic(statistics, result, "mighterium collected");
    }
  }

  private void addStatistic(List<String> statistics, Integer input, String suffix) {
    var formattedInput = STATISTIC_FORMAT.format(input);
    var statistic = String.format("%s %s", formattedInput, suffix);
    statistics.add(statistic);
  }

}
