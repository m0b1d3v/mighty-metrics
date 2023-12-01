package dev.m0b1.mighty.metrics.scorecard.parsers;

import dev.m0b1.mighty.metrics.db.scorecard.DbScoreCard;
import dev.m0b1.mighty.metrics.scorecard.ImageText;
import org.apache.commons.collections4.ListUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceImageTextParser2_0 extends ServiceImageTextParserBase {

  public void run(DbScoreCard dbScoreCard, List<ImageText> imageTexts) {

    removeUselessText(imageTexts);

    var indexCoach = indexOfKeywords(imageTexts, "WORKOUT TEAM - ");
    coach(dbScoreCard, imageTexts, indexCoach);

    var indexAverages = 3;
    groupAverage(dbScoreCard, imageTexts, indexAverages);
    personalAverage(dbScoreCard, imageTexts, indexAverages);

    var indexWorkoutIntensity = 4;
    workoutIntensity(dbScoreCard, imageTexts, indexWorkoutIntensity);

    var indexDate = 2;
    localDate(dbScoreCard, imageTexts, indexDate);

    var indexTime = 2;
    localTime(dbScoreCard, imageTexts, indexTime);

    int indexExerciseStart = indexOfKeywords(imageTexts, "GROUP AVERAGE") + 2;
    int indexExerciseEnd = ListUtils.indexOf(imageTexts, t -> t.getY() > 0.66) - 1;
    exercises(dbScoreCard, imageTexts, indexExerciseStart, indexExerciseEnd);

    var indexMighterium = indexOfKeywords(imageTexts, "MIGHTERIUM COLLECTED") - 2;
    mighterium(dbScoreCard, imageTexts, indexMighterium);
  }

}
