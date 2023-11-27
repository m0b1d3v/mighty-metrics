package dev.m0b1.mighty.metrics.parser;

import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BoundingPoly;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.protobuf.ByteString;
import dev.m0b1.mighty.metrics.db.scorecard.DbScoreCard;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Precision;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static net.logstash.logback.marker.Markers.append;

@RequiredArgsConstructor
@Slf4j
@Service
public class ServiceImageParser {

  /**
   * Read a processed image into a sorted map of strings with relevant top-left origin points first.
   */
  public void run(DbScoreCard dbScoreCard, MultipartFile multipartFile) {

    var annotateImageResponse = fetchGoogleImageAnnotations(multipartFile);

    var imageTexts = parse(annotateImageResponse);

    imageTextCleanup(imageTexts);
    setSimilarImageTextHeightsToEqualValues(imageTexts);
    imageTextCleanup(imageTexts);
    combineImageTextsThatShouldBeGrouped(imageTexts);
    imageTextCleanup(imageTexts);

    var scorecardVersion = parseVersion(imageTexts);
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

  private AnnotateImageResponse fetchGoogleImageAnnotations(MultipartFile multipartFile) {

    AnnotateImageResponse result = null;

    try (var imageAnnotatorClient = ImageAnnotatorClient.create()) {

      var bytes = multipartFile.getBytes();
      var byteString = ByteString.copyFrom(bytes);
      var image = Image.newBuilder().setContent(byteString).build();

      var feature = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build();

      var request = AnnotateImageRequest.newBuilder()
        .addFeatures(feature)
        .setImage(image)
        .build();

      var requests = List.of(request);
      var response = imageAnnotatorClient.batchAnnotateImages(requests);
      var responses = response.getResponsesList();
      result = responses.get(0);

    } catch (IOException e) {
      log.atError()
        .setMessage("Could not fetch Google image annotations")
        .setCause(e)
        .addMarker(append("originalFileName", multipartFile.getOriginalFilename()))
        .log();
    }

    return result;
  }

  private List<ImageText> parse(AnnotateImageResponse annotatedImageResponse) {

    var result = new LinkedList<ImageText>();

    // TODO: Get image width and height, preferably without annotatedImageResponse.getFullTextAnnotation.getPagesList().get(0).getWidth/Height()

    if (annotatedImageResponse != null) {
      for (var textAnnotation : annotatedImageResponse.getTextAnnotationsList()) {
        var imageText = determineImageTextPosition(textAnnotation.getBoundingPoly(), 1440, 2560);
        imageText.setValue(textAnnotation.getDescription());
        result.add(imageText);
      }
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

  private void setSimilarImageTextHeightsToEqualValues(List<ImageText> imageTexts) {
    for (int i = 0; i < imageTexts.size() - 1; i++) {

      var currentImageText = imageTexts.get(i);
      var nextImageText = imageTexts.get(i + 1);

      if (Precision.equals(currentImageText.getY(), nextImageText.getY(), 0.005)) {
        nextImageText.setY(currentImageText.getY());
      }
    }
  }

  private void combineImageTextsThatShouldBeGrouped(List<ImageText> imageTexts) {
    var iterator = imageTexts.listIterator();

    while (iterator.hasNext()) {

      var currentImageText = iterator.next();

      if (iterator.hasNext()) {

        var nextImageText = iterator.next();

        if (Precision.equals(currentImageText.getY(), nextImageText.getY(), 0.005) && (
          currentImageText.getY() < 0.3125 // Above exercises
            || currentImageText.getY() > .66406 // Below exercises
            || Math.round(currentImageText.getX()) == Math.round(nextImageText.getX()) // Same half of image
        )) {
          var combinedValue = STR."\{currentImageText.getValue()} \{nextImageText.getValue()}";
          currentImageText.setValue(combinedValue);
          iterator.remove();
        }

        iterator.previous();
      }
    }
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

  private String findMatchingGroup(String value, String regex, int desiredGroup) {

    String result = null;

    var pattern = Pattern.compile(regex);
    var matcher = pattern.matcher(value);

    if (matcher.find() && matcher.groupCount() >= desiredGroup) {
      result = matcher.group(desiredGroup);
    }

    return result;
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

  private Optional<ImageText> findByNormalizedPosition(List<ImageText> imageTexts, double y) {
    return imageTexts
      .stream()
      .filter(text -> text.getY() == y)
      .findFirst();
  }

  private boolean isVersion1(String scorecardVersion) {
    return StringUtils.startsWith(scorecardVersion, "1.");
  }

}
