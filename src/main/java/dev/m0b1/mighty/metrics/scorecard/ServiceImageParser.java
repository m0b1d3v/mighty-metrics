package dev.m0b1.mighty.metrics.scorecard;

import dev.m0b1.mighty.metrics.db.scorecard.DbScoreCard;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.math3.util.Precision;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Service
public class ServiceImageParser {

  private static final String PATTERN_SCORE = "([A-Z]\\s?\\+?)";

  /**
   * Read a processed image into a sorted map of strings with relevant top-left origin points first.
   */
  public void run(DbScoreCard dbScoreCard, List<ImageText> imageTexts) {

    parseVersion(dbScoreCard, imageTexts);

    if (isVersion2(dbScoreCard)) {
      imageTexts.removeIf(imageText -> StringUtils.endsWithAny(imageText.getValue(), "Generalised Tracking", " Workout"));
    }

    var coach = parseCoach(dbScoreCard, imageTexts);
    var groupAverage = parseGroupAverage(dbScoreCard, imageTexts);
    var personalAverage = parsePersonalAverage(dbScoreCard, imageTexts);
    var workoutIntensity = parseWorkoutIntensity(dbScoreCard, imageTexts);
    var localDate = parseLocalDate(dbScoreCard, imageTexts);
    var localTime = parseLocalTime(dbScoreCard, imageTexts);
    var mighterium = parseMighterium(dbScoreCard, imageTexts);
    var exercises = parseExercises(dbScoreCard, imageTexts);

    var breakpoint = 1;
  }

  private void parseVersion(DbScoreCard dbScoreCard, List<ImageText> imageTexts) {

    var keywords = "The Mighty Gym v";

    var string = imageTexts
      .stream()
      .filter(text -> text.getValue().contains(keywords))
      .findFirst()
      .map(text -> StringUtils.substringAfter(text.getValue(), keywords))
      .orElse(null);

    var result = NumberUtils.createDouble(string);

    dbScoreCard.setVersion(result);
  }

  private String parseCoach(DbScoreCard dbScoreCard, List<ImageText> imageTexts) {

    String result = null;

    if ( ! isVersion1(dbScoreCard)) {
      result = findByNormalizedPosition(imageTexts, 0.17188)
        .map(text -> StringUtils.substringAfter(text.getValue(), "Operator : "))
        .orElse(null);
    }

    return result;
  }

  private String parseGroupAverage(DbScoreCard dbScoreCard, List<ImageText> imageTexts) {

    String result;

    double yPosition;
    if (isVersion1(dbScoreCard)) {
      yPosition = 0.16016;
    } else if (isVersion2_0(dbScoreCard)) {
      yPosition = 0.13021;
    } else {
      yPosition = 0.20313;
    }

    var regex = STR."^\{PATTERN_SCORE}";
    result = findByNormalizedPosition(imageTexts, yPosition)
      .map(text -> findMatchingGroup(text.getValue(), regex, 1))
      .orElse(null);

    return formatScore(result);
  }

  private String parsePersonalAverage(DbScoreCard dbScoreCard, List<ImageText> imageTexts) {

    String result;

    if (isVersion1(dbScoreCard)) {

      var regex = STR."^PERSONAL \{PATTERN_SCORE}";

      return imageTexts
        .stream()
        .filter(text -> StringUtils.startsWith(text.getValue(), "PERSONAL "))
        .findFirst()
        .map(text -> findMatchingGroup(text.getValue(), regex, 1))
        .orElse(null);

    } else {

      var regex = STR."^\{PATTERN_SCORE}\\s*\{PATTERN_SCORE}";

      double yPosition;
      if (isVersion2_0(dbScoreCard)) {
        yPosition = 0.13021;
      } else {
        yPosition = 0.20313;
      }

      result = findByNormalizedPosition(imageTexts, yPosition)
        .map(text -> findMatchingGroup(text.getValue(), regex, 2))
        .orElse(null);
    }

    return formatScore(result);
  }

  private String parseWorkoutIntensity(DbScoreCard dbScoreCard, List<ImageText> imageTexts) {

    var intensityPattern = "(\\d+)";

    String regex;
    double yPosition;
    if (isVersion1(dbScoreCard)) {
      regex = STR."^INTENSITY\\s*\{intensityPattern}";
      yPosition = 0.28125;
    } else {

      regex = STR."^\{intensityPattern}";

      if (isVersion2_0(dbScoreCard)) {
        yPosition = 0.14063;
      } else {
        yPosition = 0.21484;
      }
    }

    return findByNormalizedPosition(imageTexts, yPosition)
      .map(text -> findMatchingGroup(text.getValue(), regex, 1))
      .orElse(null);
  }

  private String parseLocalDate(DbScoreCard dbScoreCard, List<ImageText> imageTexts) {

    double yPosition;
    if (isVersion1(dbScoreCard)) {
      yPosition = 0.09766;
    } else if (isVersion2_0(dbScoreCard)) {
      yPosition = 0.08333;
    } else {
      yPosition = 0.13281;
    }

    return findByNormalizedPosition(imageTexts, yPosition)
      .map(ImageText::getValue)
      .orElse(null);
  }

  private String parseLocalTime(DbScoreCard dbScoreCard, List<ImageText> imageTexts) {

    double yPosition;
    if (isVersion1(dbScoreCard)) {
      yPosition = 0.09766;
    } else if (isVersion2_0(dbScoreCard)) {
      yPosition = 0.08333;
    } else {
      yPosition = 0.14844;
    }

    return findByNormalizedPosition(imageTexts, yPosition)
      .map(ImageText::getValue)
      .orElse(null);
  }

  private String parseMighterium(DbScoreCard dbScoreCard, List<ImageText> imageTexts) {

    String result = null;

    if ( ! isVersion1(dbScoreCard)) {

      double yPosition;
      if (isVersion2_0(dbScoreCard)) {
        yPosition = 0.18229;
      } else {
        yPosition = 0.25781;
      }

      result = findByNormalizedPosition(imageTexts, yPosition)
        .map(ImageText::getValue)
        .orElse(null);
    }

    return result;
  }

  private List<String> parseExercises(DbScoreCard dbScoreCard, List<ImageText> imageTexts) {

    var result = new LinkedList<String>();

    double yCutoff;
    if (isVersion1(dbScoreCard)) {
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

  private String formatScore(String score) {
    return RegExUtils.replaceAll(score, "\\s", "");
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

  private boolean isVersion1(DbScoreCard dbScoreCard) {
    return dbScoreCard.getVersion() != null && dbScoreCard.getVersion() < 2;
  }

  private boolean isVersion2(DbScoreCard dbScoreCard) {
    return dbScoreCard.getVersion() != null && dbScoreCard.getVersion() >= 2;
  }

  private boolean isVersion2_0(DbScoreCard dbScoreCard) {
    return dbScoreCard.getVersion() != null && Precision.equals(dbScoreCard.getVersion(), 2);
  }

}
