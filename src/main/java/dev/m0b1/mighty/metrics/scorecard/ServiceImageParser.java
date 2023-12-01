package dev.m0b1.mighty.metrics.scorecard;

import dev.m0b1.mighty.metrics.db.scorecard.DbScoreCard;
import dev.m0b1.mighty.metrics.scorecard.parsers.ServiceImageTextParser1;
import dev.m0b1.mighty.metrics.scorecard.parsers.ServiceImageTextParser2_0;
import dev.m0b1.mighty.metrics.scorecard.parsers.ServiceImageTextParser2_1;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ServiceImageParser {

  private final ServiceImageTextParser1 serviceImageTextParser1;
  private final ServiceImageTextParser2_0 serviceImageTextParser2_0;
  private final ServiceImageTextParser2_1 serviceImageTextParser2_1;

  /**
   * Read a processed image into a sorted map of strings with relevant top-left origin points first.
   */
  public void run(DbScoreCard dbScoreCard, List<ImageText> imageTexts) {

    parseVersion(dbScoreCard, imageTexts);

    if (dbScoreCard.getVersion() != null) {
      if (dbScoreCard.getVersion() < 2) {
        serviceImageTextParser1.run(dbScoreCard, imageTexts);
      } else if (dbScoreCard.getVersion() == 2.0) {
        serviceImageTextParser2_0.run(dbScoreCard, imageTexts);
      } else {
        serviceImageTextParser2_1.run(dbScoreCard, imageTexts);
      }
    }
  }

  private void parseVersion(DbScoreCard dbScoreCard, List<ImageText> imageTexts) {

    var keywords = "The Mighty Gym v";
    var index = ListUtils.indexOf(imageTexts, t -> StringUtils.contains(t.getValue(), keywords));
    var imageText = IterableUtils.get(imageTexts, index);

    if (imageText != null) {
      var string = StringUtils.substringAfter(imageText.getValue(), keywords);
      var result = NumberUtils.createDouble(string);
      dbScoreCard.setVersion(result);
    }
  }

}
