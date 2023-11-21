package dev.m0b1.mighty.metrics.statistics;

import dev.m0b1.mighty.metrics.db.scorecard.DbScoreCard;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;

@Service
public class ServiceStatistics {

  private static final DecimalFormat STATISTIC_FORMAT = new DecimalFormat("#,###");

  public List<String> generate(List<DbScoreCard> dbScoreCards) {

    var result = new LinkedList<String>();

    if (dbScoreCards == null || dbScoreCards.isEmpty()) {
      result.add("No statistics yet, go do some arm circles!");
    } else {
      totalWorkouts(result, dbScoreCards);
      score(result, dbScoreCards);
      workoutIntensity(result, dbScoreCards);
      topRank(result, dbScoreCards);
      groupRank(result, dbScoreCards);
      mighteriumCollected(result, dbScoreCards);
    }

    return result;
  }

  private void totalWorkouts(List<String> statistics, List<DbScoreCard> dbScoreCards) {
    var result = dbScoreCards.size();
    if (result > 2) {
      add(statistics, result, "workout sessions");
    }
  }

  private void score(List<String> statistics, List<DbScoreCard> dbScoreCards) {

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
        add(statistics, result, "average personal score");
      }
    }
  }

  private void workoutIntensity(List<String> statistics, List<DbScoreCard> dbScoreCards) {

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
      add(statistics, result, "average workout intensity");
    }
  }

  private void topRank(List<String> statistics, List<DbScoreCard> dbScoreCards) {

    var result = 0;

    for (var card : dbScoreCards) {
      if (card.getIdScorePersonal() != null && card.getIdScorePersonal() > 3) {
        result++;
      }
    }

    if (result > 1) {
      add(statistics, result, "S ranks earned");
    }
  }

  private void groupRank(List<String> statistics, List<DbScoreCard> dbScoreCards) {

    var result = 0;

    for (var card : dbScoreCards) {
      var scorePersonal = card.getIdScorePersonal();
      var scoreGroup = card.getIdScoreGroup();
      if (scorePersonal != null && scoreGroup != null && scorePersonal <= scoreGroup) {
        result++;
      }
    }

    if (result > 0) {
      add(statistics, result, "group workout gold stars");
    }
  }

  private void mighteriumCollected(List<String> statistics, List<DbScoreCard> dbScoreCards) {

    var result = 0;

    for (var card : dbScoreCards) {
      if (card.getMighteriumCollected() != null) {
        result += card.getMighteriumCollected();
      }
    }

    if (result > 0) {
      add(statistics, result, "mighterium collected");
    }
  }

  private void add(List<String> statistics, Integer input, String suffix) {
    var formattedInput = STATISTIC_FORMAT.format(input);
    var statistic = STR."\{formattedInput} \{suffix}";
    statistics.add(statistic);
  }

}
