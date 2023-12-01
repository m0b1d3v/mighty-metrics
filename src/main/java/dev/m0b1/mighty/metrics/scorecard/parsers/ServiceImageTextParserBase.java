package dev.m0b1.mighty.metrics.scorecard.parsers;

import dev.m0b1.mighty.metrics.db.scorecard.DbScoreCard;
import dev.m0b1.mighty.metrics.db.scorecard.DbScoreCardExercise;
import dev.m0b1.mighty.metrics.scorecard.ImageText;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public abstract class ServiceImageTextParserBase {

  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH);
  private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH);

  private static final String PATTERN_SCORE = "([A-Z]\\s?\\+?)";

  protected void removeUselessText(List<ImageText> imageTexts) {
    imageTexts.removeIf(imageText -> StringUtils.endsWithAny(
      imageText.getValue(),
      "Stretching",
      "Tracking",
      "Workout"
    ));
  }

  protected Integer indexOfKeywords(List<ImageText> imageTexts, String keywords) {
    return ListUtils.indexOf(imageTexts, t -> StringUtils.contains(t.getValue(), keywords));
  }

  protected String imageTextAtIndex(List<ImageText> imageTexts, int index) {

    String result = null;

    if (index >= 0 && index < imageTexts.size()) {
      var imageText = imageTexts.get(index);
      result = imageText.getValue();
    }

    return result;
  }

  protected Integer determineScoreId(String value) {

    value = RegExUtils.replaceAll(value, "\\s", "");
    value = StringUtils.upperCase(value);
    value = StringUtils.defaultString(value);

    return switch (value) {
      case "S+" -> 1;
      case "S" -> 2;
      case "A" -> 3;
      case "B" -> 4;
      case "C" -> 5;
      case "D" -> 6;
      default -> null;
    };
  }

  protected Integer findScoreByRegex(String regex, List<ImageText> imageTexts, int index) {

    Integer result = null;

    var imageText = imageTextAtIndex(imageTexts, index);
    if (StringUtils.isNotEmpty(imageText)) {
      var match = findFirstMatchingGroup(imageText, regex);
      result = determineScoreId(match);
    }

    return result;
  }

  protected Integer findFirstIntegerByRegex(List<ImageText> imageTexts, int index) {

    Integer result = null;

    var imageText = imageTextAtIndex(imageTexts, index);
    if (StringUtils.isNotEmpty(imageText)) {

      var match = findFirstMatchingGroup(imageText, "(\\d+)");

      if (StringUtils.isNumeric(match)) {
        result = NumberUtils.createInteger(match);
      }
    }

    return result;
  }

  protected String findFirstMatchingGroup(String value, String regex) {

    String result = null;

    var pattern = Pattern.compile(regex);
    var matcher = pattern.matcher(value);

    if (matcher.find() && matcher.groupCount() >= 1) {
      result = matcher.group(1);
    }

    return result;
  }

  protected void coach(DbScoreCard dbScoreCard, List<ImageText> imageTexts, int index) {

    var imageText = imageTextAtIndex(imageTexts, index);
    if (StringUtils.containsAnyIgnoreCase(imageText, "Coach", "Kugo")) {
      dbScoreCard.setIdCoach(1);
    } else if (StringUtils.containsAnyIgnoreCase(imageText, "Ayumi", "Koi")) {
      dbScoreCard.setIdCoach(2);
    } else if (StringUtils.containsAnyIgnoreCase(imageText, "Bossun")) {
      dbScoreCard.setIdCoach(3);
    }
  }

  protected void groupAverage(DbScoreCard dbScoreCard, List<ImageText> imageTexts, int index) {
    var result = findScoreByRegex(STR."^\{PATTERN_SCORE}", imageTexts, index);
    dbScoreCard.setIdScorePersonal(result);
  }

  protected void personalAverage(DbScoreCard dbScoreCard, List<ImageText> imageTexts, int index) {
    var result = findScoreByRegex(STR."\{PATTERN_SCORE}$", imageTexts, index);
    dbScoreCard.setIdScorePersonal(result);
  }

  protected void workoutIntensity(DbScoreCard dbScoreCard, List<ImageText> imageTexts, int index) {
    var result = findFirstIntegerByRegex(imageTexts, index);
    dbScoreCard.setWorkoutIntensity(result);
  }

  protected void localDate(DbScoreCard dbScoreCard, List<ImageText> imageTexts, int index) {

    var imageText = imageTextAtIndex(imageTexts, index);
    if (StringUtils.isNotEmpty(imageText)) {

      var regex = "(\\d{1,2}\\s\\p{Alpha}+\\s\\d{4})";
      var match = findFirstMatchingGroup(imageText, regex);

      if (match != null) {
        try {
          var result = LocalDate.parse(match, DATE_FORMATTER);
          dbScoreCard.setDate(result);
        } catch (DateTimeParseException e) {
          // Intentionally empty
        }
      }
    }
  }

  protected void localTime(DbScoreCard dbScoreCard, List<ImageText> imageTexts, int index) {

    var imageText = imageTextAtIndex(imageTexts, index);
    if (StringUtils.isNotEmpty(imageText)) {

      var regex = "(\\d{1,2}:\\d{2})";
      var match = findFirstMatchingGroup(imageText, regex);

      if (match != null) {
        try {
          var result = LocalTime.parse(match, TIME_FORMATTER);
          dbScoreCard.setTime(result);
        } catch (DateTimeParseException e) {
          // Intentionally empty
        }
      }
    }
  }

  protected void mighterium(DbScoreCard dbScoreCard, List<ImageText> imageTexts, int index) {
    var result = findFirstIntegerByRegex(imageTexts, index);
    dbScoreCard.setMighteriumCollected(result);
  }

  protected List<ImageText> exercises(DbScoreCard dbScoreCard, List<ImageText> imageTexts, int start, int end) {

    dbScoreCard.getExercises().clear();

    var exercises = determineExerciseImageText(imageTexts, start, end);

    for (var exercise : exercises) {
      var result = createDbScoreCardExercise(exercise);
      dbScoreCard.getExercises().add(result);
    }

    return exercises;
  }

  private List<ImageText> determineExerciseImageText(List<ImageText> imageTexts, int start, int end) {

    var exercises = new LinkedList<ImageText>();

    for (var i = 0; i < imageTexts.size(); i++) {
      if (i >= start && i <= end) {
        exercises.add(imageTexts.get(i));
      }
    }

    exercises.sort(imageTextExerciseOrderComparator());

    return exercises;
  }

  private Comparator<ImageText> imageTextExerciseOrderComparator() {
    return Comparator
      .comparingDouble((ImageText t) -> Math.round(t.getX()))
      .thenComparingDouble(ImageText::getY);
  }

  private DbScoreCardExercise createDbScoreCardExercise(ImageText imageText) {

    var name = imageText.getValue();
    name = formatExercise(name);

    var regex = STR." \{PATTERN_SCORE}$";
    var score = findFirstMatchingGroup(name, regex);
    var scoreId = determineScoreId(score);

    name = StringUtils.removeEndIgnoreCase(name, score);
    name = StringUtils.trim(name);

    var result = new DbScoreCardExercise();
    result.setExercise(name);
    result.setIdScore(scoreId);

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

}
