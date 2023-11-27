package dev.m0b1.mighty.metrics.parser;

import dev.m0b1.mighty.metrics.db.scorecard.DbScoreCard;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Service
public class ServiceImageParser {

  /**
   * Read a processed image into a sorted map of strings with relevant top-left origin points first.
   */
  public void run(DbScoreCard dbScoreCard, List<ImageText> imageTexts) {

    var scorecardVersion = parseVersion(imageTexts);

    if (isVersion2(scorecardVersion)) {
      imageTexts.removeIf(imageText -> StringUtils.endsWithAny(imageText.getValue(), "Generalised Tracking", " Workout"));
    }

    var coach = parseCoach(scorecardVersion, imageTexts);
    var groupAverage = parseGroupAverage(scorecardVersion, imageTexts);
    var personalAverage = parsePersonalAverage(scorecardVersion, imageTexts);
    var workoutIntensity = parseWorkoutIntensity(scorecardVersion, imageTexts);
    var localDate = parseLocalDate(scorecardVersion, imageTexts);
    var localTime = parseLocalTime(scorecardVersion, imageTexts);
    var mighterium = parseMighterium(scorecardVersion, imageTexts);
    var exercises = parseExercises(scorecardVersion, imageTexts);

    var breakpoint = 1;
  }

  private String parseVersion(List<ImageText> imageTexts) {

    var keywords = "The Mighty Gym v";

    return imageTexts
      .stream()
      .filter(text -> text.getValue().contains(keywords))
      .findFirst()
      .map(text -> StringUtils.substringAfter(text.getValue(), keywords))
      .orElse(null);
  }

  private String parseCoach(String scorecardVersion, List<ImageText> imageTexts) {

    String result = null;

    if ( ! isVersion1(scorecardVersion)) {
      result = findByNormalizedPosition(imageTexts, 0.17188)
        .map(text -> StringUtils.substringAfter(text.getValue(), "Operator : "))
        .orElse(null);
    }

    return result;
  }

  private String parseGroupAverage(String scorecardVersion, List<ImageText> imageTexts) {

    var scorePattern = "([A-Z]\\+?)";

    double yPosition;
    if (isVersion1(scorecardVersion)) {
      yPosition = 0.16016;
    } else {
      yPosition = 0.20703;
    }

    var regex = STR."^\{scorePattern}";
    return findByNormalizedPosition(imageTexts, yPosition)
      .map(text -> findMatchingGroup(text.getValue(), regex, 1))
      .orElse(null);
  }

  private String parsePersonalAverage(String scorecardVersion, List<ImageText> imageTexts) {

    String result;
    var scorePattern = "([A-Z]\\+?)";

    if (isVersion1(scorecardVersion)) {

      var regex = STR."^PERSONAL \{scorePattern}";

      return imageTexts
        .stream()
        .filter(text -> StringUtils.startsWith(text.getValue(), "PERSONAL "))
        .findFirst()
        .map(text -> findMatchingGroup(text.getValue(), regex, 1))
        .orElse(null);

    } else {

      var regex = STR."^\{scorePattern}\\s*\{scorePattern}";

      result = findByNormalizedPosition(imageTexts, 0.20703)
        .map(text -> findMatchingGroup(text.getValue(), regex, 2))
        .orElse(null);
    }

    return result;
  }

  private String parseWorkoutIntensity(String scorecardVersion, List<ImageText> imageTexts) {

    var intensityPattern = "(\\d+)";

    String regex;
    double yPosition;
    if (isVersion1(scorecardVersion)) {
      regex = STR."^INTENSITY\\s*\{intensityPattern}";
      yPosition = 0.28125;
    } else {
      regex = STR."^\{intensityPattern}";
      yPosition = 0.21875;
    }

    return findByNormalizedPosition(imageTexts, yPosition)
      .map(text -> findMatchingGroup(text.getValue(), regex, 1))
      .orElse(null);
  }

  private String parseLocalDate(String scorecardVersion, List<ImageText> imageTexts) {

    double yPosition;
    if (isVersion1(scorecardVersion)) {
      yPosition = 0.09766;
    } else {
      yPosition = 0.13672;
    }

    return findByNormalizedPosition(imageTexts, yPosition)
      .map(ImageText::getValue)
      .orElse(null);
  }

  private String parseLocalTime(String scorecardVersion, List<ImageText> imageTexts) {

    double yPosition;
    if (isVersion1(scorecardVersion)) {
      yPosition = 0.09766;
    } else {
      yPosition = 0.14453;
    }

    return findByNormalizedPosition(imageTexts, yPosition)
      .map(ImageText::getValue)
      .orElse(null);
  }

  private String parseMighterium(String scorecardVersion, List<ImageText> imageTexts) {

    String result = null;

    if ( ! isVersion1(scorecardVersion)) {
      result = findByNormalizedPosition(imageTexts, 0.25781)
        .map(ImageText::getValue)
        .orElse(null);
    }

    return result;
  }

  private List<String> parseExercises(String scorecardVersion, List<ImageText> imageTexts) {

    var result = new LinkedList<String>();

    double yCutoff;
    if (isVersion1(scorecardVersion)) {
      yCutoff = 0.66400;
    } else {
      yCutoff = 0.66406;
    }

    var exerciseText = imageTexts
      .stream()
      .filter(text -> text.getY() >= 0.3125)
      .filter(text -> text.getY() <= yCutoff)
      .toList();

    exerciseText.stream().filter(text -> text.getX() < 0.5).forEach(text -> result.add(text.getValue()));
    exerciseText.stream().filter(text -> text.getX() >= 0.5).forEach(text -> result.add(text.getValue()));

    result.replaceAll(this::formatExercise);

    return result;
  }

  private String formatExercise(String exercise) {

    // Plus sign preceded by a space means it's part of a score, remove the space
    exercise = exercise.replaceAll(" \\+", "+");

    // If a master level exercise has a yellow outline, it could appear double bracketed
    exercise = exercise.replaceAll("^\\[ \\[", "[");

    // Brackets don't need to be unnecessarily spaced
    exercise = exercise.replaceAll("\\[ ", "[");
    exercise = exercise.replaceAll(" ]", "]");

    // Parenthesis don't need to be unnecessarily spaced
    exercise = exercise.replaceAll("\\( ", "(");
    exercise = exercise.replaceAll(" \\)", ")");

    // Google loves to turn C scores lowercase, make them uppercase
    // Apparently this is not a regular c character, we love encoding
    exercise = exercise.replaceAll(" с", " C");

    // Version 1.x used hyphens to separate exercises from scores
    // Yes, these are different hyphens, we love encoding
    exercise = exercise.replaceAll(" – ", " ");
    exercise = exercise.replaceAll(" - ", " ");

    return exercise;
  }

  private String findMatchingGroup(String value, String regex, int desiredGroup) {

    String result = null;

    var pattern = Pattern.compile(regex);
    var matcher = pattern.matcher(value);

    if (matcher.find() && matcher.groupCount() >= desiredGroup) {
      result = matcher.group(desiredGroup);
    }

    return result;
  }

  private Optional<ImageText> findByNormalizedPosition(List<ImageText> imageTexts, double y) {
    return imageTexts
      .stream()
      .filter(text -> text.getY() == y)
      .findFirst();
  }

  private boolean isVersion1(String scorecardVersion) {
    return StringUtils.startsWith(scorecardVersion, "1.");
  }

  private boolean isVersion2(String scorecardVersion) {
    return StringUtils.startsWith(scorecardVersion, "2.");
  }

}
