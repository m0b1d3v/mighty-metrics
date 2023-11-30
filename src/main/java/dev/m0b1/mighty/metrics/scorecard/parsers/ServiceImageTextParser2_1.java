package dev.m0b1.mighty.metrics.scorecard.parsers;

import dev.m0b1.mighty.metrics.db.scorecard.DbScoreCard;
import dev.m0b1.mighty.metrics.scorecard.ImageText;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.ListUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ServiceImageTextParser2_1 extends ServiceImageTextParserBase {

  public void run(DbScoreCard dbScoreCard, List<ImageText> imageTexts) {

    removeUselessText(imageTexts);

    var indexCoach = indexOfKeywords(imageTexts, "Operator : ");
    var indexExerciseStart = indexOfKeywords(imageTexts, "MIGHTERIUM COLLECTED") + 1;
    var indexExerciseEnd = ListUtils.indexOf(imageTexts, t -> t.getY() > 0.66) - 1;

    var coach = imageTextAtIndex(imageTexts, indexCoach);
    var groupAverage = imageTextAtIndex(imageTexts, indexCoach + 1);
    var personalAverage = imageTextAtIndex(imageTexts, indexCoach + 1);
    var workoutIntensity = imageTextAtIndex(imageTexts, indexCoach + 2);
    var localDate = imageTextAtIndex(imageTexts, indexCoach - 3);
    var localTime = imageTextAtIndex(imageTexts, indexCoach - 2);
    var mighterium = imageTextAtIndex(imageTexts, indexExerciseStart - 3);
    var exercises = exercises(imageTexts, indexExerciseStart, indexExerciseEnd);

    var breakpoint = 1;
  }

}
