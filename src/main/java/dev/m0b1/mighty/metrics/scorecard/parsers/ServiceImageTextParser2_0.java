package dev.m0b1.mighty.metrics.scorecard.parsers;

import dev.m0b1.mighty.metrics.db.scorecard.DbScoreCard;
import dev.m0b1.mighty.metrics.scorecard.ImageText;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ServiceImageTextParser2_0 extends ServiceImageTextParserBase {

  public void run(DbScoreCard dbScoreCard, List<ImageText> imageTexts) {

    removeUselessText(imageTexts);

    var indexDate = 2; // Pray this is consistent
    var indexCoach = indexOfKeywords(imageTexts, "WORKOUT TEAM - ");
    var indexMighterium = indexOfKeywords(imageTexts, "MIGHTERIUM COLLECTED"); // Not always present
    var indexExerciseStart = indexOfKeywords(imageTexts, "GROUP AVERAGE") + 2;
    var indexExerciseEnd = ListUtils.indexOf(imageTexts, t -> t.getY() > 0.66) - 1;

    var coach = imageTextAtIndex(imageTexts, indexCoach);
    var groupAverage = imageTextAtIndex(imageTexts, indexDate + 1);
    var personalAverage = imageTextAtIndex(imageTexts, indexDate + 1);
    var workoutIntensity = imageTextAtIndex(imageTexts, indexDate + 2);
    // TODO: Will have to weld together dateIndex-1 and dateIndex
    var localDate = imageTextAtIndex(imageTexts, indexDate);
    var localTime = imageTextAtIndex(imageTexts, indexDate);
    var mighterium = imageTextAtIndex(imageTexts, indexMighterium - 2);
    var exercises = exercises(imageTexts, indexExerciseStart, indexExerciseEnd);

    var breakpoint = 1;
  }

}
