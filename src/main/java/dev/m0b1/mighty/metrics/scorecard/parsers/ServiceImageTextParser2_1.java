package dev.m0b1.mighty.metrics.scorecard.parsers;

import dev.m0b1.mighty.metrics.db.scorecard.DbScoreCard;
import dev.m0b1.mighty.metrics.scorecard.ImageText;
import org.apache.commons.collections4.ListUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceImageTextParser2_1 extends ServiceImageTextParserBase {

  public void run(DbScoreCard dbScoreCard, List<ImageText> imageTexts) {

    removeUselessText(imageTexts);

    var indexCoach = indexOfKeywords(imageTexts, "Operator : ");
    coach(dbScoreCard, imageTexts, indexCoach);

    var indexAverages = indexCoach + 1;
    groupAverage(dbScoreCard, imageTexts, indexAverages);
    personalAverage(dbScoreCard, imageTexts, indexAverages);

    var indexWorkoutIntensity = indexCoach + 2;
    workoutIntensity(dbScoreCard, imageTexts, indexWorkoutIntensity);

    var indexDate = indexCoach - 3;
    localDate(dbScoreCard, imageTexts, indexDate);

    var indexTime = indexCoach - 2;
    localTime(dbScoreCard, imageTexts, indexTime);

    int indexExerciseStart = indexOfKeywords(imageTexts, "MIGHTERIUM COLLECTED") + 1;
    int indexExerciseEnd = ListUtils.indexOf(imageTexts, t -> t.getY() > 0.66) - 1;
    exercises(dbScoreCard, imageTexts, indexExerciseStart, indexExerciseEnd);

    var indexMighterium = indexExerciseStart - 3;
    mighterium(dbScoreCard, imageTexts, indexMighterium);
  }

}
