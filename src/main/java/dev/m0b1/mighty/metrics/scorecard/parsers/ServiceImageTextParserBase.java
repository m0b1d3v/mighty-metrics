package dev.m0b1.mighty.metrics.scorecard.parsers;

import dev.m0b1.mighty.metrics.scorecard.ImageText;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public abstract class ServiceImageTextParserBase {

  protected static final String PATTERN_SCORE = "([A-Z]\\s?\\+?)";

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

  protected ImageText imageTextAtIndex(List<ImageText> imageTexts, int index) {

    ImageText result = null;

    if (index >= 0 && index < imageTexts.size()) {
      result = imageTexts.get(index);
    }

    return result;
  }

  protected String formatScore(String score) {
    return RegExUtils.replaceAll(score, "\\s", "");
  }

  protected String formatExercise(String exercise) {

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

  protected String findMatchingGroup(String value, String regex, int desiredGroup) {

    String result = null;

    var pattern = Pattern.compile(regex);
    var matcher = pattern.matcher(value);

    if (matcher.find() && matcher.groupCount() >= desiredGroup) {
      result = matcher.group(desiredGroup);
    }

    return result;
  }

  protected List<ImageText> exercises(List<ImageText> imageTexts, int start, int end) {

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

}
