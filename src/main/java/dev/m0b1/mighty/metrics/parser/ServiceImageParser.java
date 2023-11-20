package dev.m0b1.mighty.metrics.parser;

import com.google.cloud.spring.vision.CloudVisionTemplate;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BoundingPoly;
import com.google.cloud.vision.v1.Feature;
import dev.m0b1.mighty.metrics.db.scorecard.DbScoreCard;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Precision;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Service
public class ServiceImageParser {

  private final CloudVisionTemplate cloudVisionTemplate;

  /**
   * Read a processed image into a sorted map of strings with relevant top-left origin points first.
   */
  public DbScoreCard read(MultipartFile multipartFile) {

    var result = new DbScoreCard();

    var annotatedImageResponse = cloudVisionTemplate.analyzeImage(
      multipartFile.getResource(),
      Feature.Type.TEXT_DETECTION
    );

    var imageTexts = read(annotatedImageResponse);

    imageTextCleanup(imageTexts);

    for (int i = 0; i < imageTexts.size() - 1; i++) {

      var currentImageText = imageTexts.get(i);
      var nextImageText = imageTexts.get(i + 1);

      if (Precision.equals(currentImageText.getY(), nextImageText.getY(), 0.005)) {
        nextImageText.setY(currentImageText.getY());
      }
    }

    imageTextCleanup(imageTexts);

    var iterator = imageTexts.listIterator();

    while (iterator.hasNext()) {

      var currentImageText = iterator.next();

      if (iterator.hasNext()) {

        var nextImageText = iterator.next();

        if (Precision.equals(currentImageText.getY(), nextImageText.getY(), 0.005) && (
          currentImageText.getY() <= 0.3125 // Above exercises
          || currentImageText.getY() >= .66406 // Below exercises
          || Math.round(currentImageText.getX()) == Math.round(nextImageText.getX()) // Same half of image
        )) {
          var combinedValue = String.format("%s %s", currentImageText.getValue(), nextImageText.getValue());
          currentImageText.setValue(combinedValue);
          iterator.remove();
        }

        iterator.previous();
      }
    }

    imageTextCleanup(imageTexts);

    var scorecardVersion = parseVersion(imageTexts);
    var coach = parseCoach(imageTexts);
    var groupAverage = parseGroupAverage(imageTexts);
    var personalAverage = parsePersonalAverage(imageTexts);
    var workoutIntensity = parseWorkoutIntensity(imageTexts);
    var localDateTime = parseLocalDateTime(imageTexts);
    var mighterium = parseMighterium(imageTexts);
    var exercises = parseExercises(imageTexts);

    return result;
  }

  private List<ImageText> read(AnnotateImageResponse annotatedImageResponse) {

    var result = new LinkedList<ImageText>();

    // TODO: Get image width and height, preferably without annotatedImageResponse.getFullTextAnnotation.getPagesList().get(0).getWidth/Height()

    for (var textAnnotation : annotatedImageResponse.getTextAnnotationsList()) {
      var imageText = determineImageTextPosition(textAnnotation.getBoundingPoly(), 1440, 2560);
      imageText.setValue(textAnnotation.getDescription());
      result.add(imageText);
    }

    // The first result is always a long string with everything in it.
    result.remove(0);

    return result;
  }

  /**
   * Given a bounding box for some text in an image determine the top-left point rounded to the nearest ten pixels.
   *
   * We then normalize it to a [0, 1] bound so that comparisons are size agnostic, only orientation gnostic.
   * This rounding lets us more accurately measure text on the same "line" while accounting for character differences.
   */
  private ImageText determineImageTextPosition(BoundingPoly boundingPoly, int imageWidth, int imageHeight) {

    var result = new ImageText();

    var topLeft = boundingPoly.getVertices(0);

    result.setX(normalizeValue(topLeft.getX(), imageWidth));
    result.setY(normalizeValue(topLeft.getY(), imageHeight));

    return result;
  }

  private double normalizeValue(int value, int total) {
    var result = new BigDecimal(value);
    result = result.setScale(-1, RoundingMode.HALF_UP); // Round to nearest 10
    result = result.divide(new BigDecimal(total), 5, RoundingMode.HALF_UP); // Round to 5th decimal place
    return result.doubleValue();
  }

  private Comparator<ImageText> imageTextNaturalOrderComparator() {
    return Comparator
      .comparingDouble(ImageText::getY)
      .thenComparingDouble(ImageText::getX);
  }

  private void imageTextCleanup(List<ImageText> imageTexts) {

    imageTexts.forEach(imageText -> {
      var value = imageText.getValue();
      value = RegExUtils.replaceAll(value, "\n", " ");
      value = StringUtils.trim(value);
      imageText.setValue(value);
    });

    imageTexts.removeIf(imageText -> StringUtils.endsWithAny(imageText.getValue(), "Generalised Tracking", " Workout"));

    imageTexts.sort(imageTextNaturalOrderComparator());
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

  private String parseCoach(List<ImageText> imageTexts) {
    return findByNormalizedPosition(imageTexts, 0.8125, 0.17188)
      .map(text -> StringUtils.substringAfter(text.getValue(), "Operator : "))
      .orElse(null);
  }

  private String parseGroupAverage(List<ImageText> imageTexts) {
    var scorePattern = "([A-Z]\\+?)";
    var regex = String.format("^%s", scorePattern);
    return findByNormalizedPosition(imageTexts, 0.09028, 0.20313)
      .map(text -> findMatchingGroup(text.getValue(), regex, 1))
      .orElse(null);
  }

  private String parsePersonalAverage(List<ImageText> imageTexts) {
    var scorePattern = "([A-Z]\\+?)";
    var regex = String.format("^%s\\s*%s", scorePattern, scorePattern);
    return findByNormalizedPosition(imageTexts, 0.09028, 0.20313)
      .map(text -> findMatchingGroup(text.getValue(), regex, 2))
      .orElse(null);
  }

  private String parseWorkoutIntensity(List<ImageText> imageTexts) {
    var scorePattern = "([A-Z]\\+?)";
    var regex = String.format("^%s\\s*%s\\s*(\\d+)", scorePattern, scorePattern);
    return findByNormalizedPosition(imageTexts, 0.09028, 0.20313)
      .map(text -> findMatchingGroup(text.getValue(), regex, 3))
      .orElse(null);
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

  private String parseLocalDateTime(List<ImageText> imageTexts) {
    return findByNormalizedPosition(imageTexts, 0.18056, 0.13672)
      .map(ImageText::getValue)
      .orElse(null);
  }

  private String parseMighterium(List<ImageText> imageTexts) {
    return findByNormalizedPosition(imageTexts, 0.84722, 0.25781)
      .map(ImageText::getValue)
      .orElse(null);
  }

  private List<String> parseExercises(List<ImageText> imageTexts) {

    var result = new LinkedList<String>();

    var exerciseText = imageTexts
      .stream()
      .filter(text -> text.getY() >= 0.3125)
      .filter(text -> text.getY() <= 0.66406)
      .toList();

    exerciseText.stream().filter(text -> text.getX() < 0.5).forEach(text -> result.add(text.getValue()));
    exerciseText.stream().filter(text -> text.getX() >= 0.5).forEach(text -> result.add(text.getValue()));

    return result;
  }

  private Optional<ImageText> findByNormalizedPosition(List<ImageText> imageTexts, double x, double y) {
    return imageTexts
      .stream()
      .filter(text -> text.getX() == x)
      .filter(text -> text.getY() == y)
      .findFirst();
  }

}
