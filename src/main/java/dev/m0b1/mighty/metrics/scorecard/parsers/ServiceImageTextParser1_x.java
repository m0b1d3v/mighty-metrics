package dev.m0b1.mighty.metrics.scorecard.parsers;

import dev.m0b1.mighty.metrics.db.scorecard.DbScoreCard;
import dev.m0b1.mighty.metrics.scorecard.ImageText;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.ListUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ServiceImageTextParser1_x extends ServiceImageTextParserBase {

  public void run(DbScoreCard dbScoreCard, List<ImageText> imageTexts) {

    var indexDate = 3; // Pray this is consistent
    var indexPersonalAverage = indexOfKeywords(imageTexts, "PERSONAL ");
    var indexIntensity = indexOfKeywords(imageTexts, "INTENSITY ");
    var indexExerciseStart = ListUtils.indexOf(imageTexts, t -> t.getY() > 0.32);
    var indexExerciseEnd = ListUtils.indexOf(imageTexts, t -> t.getY() > 0.66) - 1;

    var groupAverage = imageTextAtIndex(imageTexts, indexDate + 1);
    var personalAverage = imageTextAtIndex(imageTexts, indexPersonalAverage);
    var workoutIntensity = imageTextAtIndex(imageTexts, indexIntensity);
    // TODO: Will have to weld together dateIndex-1 and dateIndex
    var localDate = imageTextAtIndex(imageTexts, indexDate);
    var localTime = imageTextAtIndex(imageTexts, indexDate);
    var exercises = exercises(imageTexts, indexExerciseStart, indexExerciseEnd);

    var breakpoint = 1;
  }

}
