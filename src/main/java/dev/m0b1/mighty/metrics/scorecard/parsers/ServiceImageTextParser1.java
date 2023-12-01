package dev.m0b1.mighty.metrics.scorecard.parsers;

import dev.m0b1.mighty.metrics.db.scorecard.DbScoreCard;
import dev.m0b1.mighty.metrics.scorecard.ImageText;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.ListUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ServiceImageTextParser1 extends ServiceImageTextParserBase {

  public void run(DbScoreCard dbScoreCard, List<ImageText> imageTexts) {

    var indexGroupAverage = 4;
    groupAverage(dbScoreCard, imageTexts, indexGroupAverage);

    var indexPersonalAverage = indexOfKeywords(imageTexts, "PERSONAL ");
    personalAverage(dbScoreCard, imageTexts, indexPersonalAverage);

    var indexWorkoutIntensity = indexOfKeywords(imageTexts, "INTENSITY ");
    workoutIntensity(dbScoreCard, imageTexts, indexWorkoutIntensity);

    var indexDateAndTime = 3;
    localDate(dbScoreCard, imageTexts, indexDateAndTime);
    localTime(dbScoreCard, imageTexts, indexDateAndTime);

    var indexExerciseStart = ListUtils.indexOf(imageTexts, t -> t.getY() > 0.32);
    var indexExerciseEnd = ListUtils.indexOf(imageTexts, t -> t.getY() > 0.66) - 1;
    exercises(dbScoreCard, imageTexts, indexExerciseStart, indexExerciseEnd);
  }

}
