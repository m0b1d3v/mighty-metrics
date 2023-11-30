package dev.m0b1.mighty.metrics.scorecard;

import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BoundingPoly;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Precision;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Logic that should be run for any scorecard regardless of version.
 *
 * Version specific logic should go later down the service chain, for example the parser.
 */
@Slf4j
@Service
public class ServiceImageOcr {

  private static final double Y_HEIGHT_DIFFERENCE_CONSIDERED_FOR_SAME_LINE = 0.005;

  /**
   * Read a processed image into a sorted map of strings with relevant top-left origin points first.
   */
  public List<ImageText> run(byte[] bytes) {

    var annotateImageResponse = fetchGoogleImageAnnotations(bytes);
    var imageTexts = read(annotateImageResponse);

    imageTextCleanup(imageTexts);
    setSimilarImageTextHeightsToEqualValues(imageTexts);
    imageTextCleanup(imageTexts);
    combineImageTextsThatShouldBeGrouped(imageTexts);
    imageTextCleanup(imageTexts);

    return imageTexts;
  }

  /**
   * Send out an image to Google for them to run text detection.
   */
  private AnnotateImageResponse fetchGoogleImageAnnotations(byte[] bytes) {

    AnnotateImageResponse result = null;

    try (var imageAnnotatorClient = ImageAnnotatorClient.create()) {

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
        .log();
    }

    return result;
  }

  /**
   * Read the normalized top-left point and text out of all the data points Google gives us.
   */
  private List<ImageText> read(AnnotateImageResponse annotatedImageResponse) {

    var result = new LinkedList<ImageText>();

    if (annotatedImageResponse != null) {

      var firstPage = annotatedImageResponse.getFullTextAnnotation().getPagesList().get(0);
      var imageWidth = firstPage.getWidth();
      var imageHeight = firstPage.getHeight();

      for (var textAnnotation : annotatedImageResponse.getTextAnnotationsList()) {
        var imageText = determineImageTextPosition(textAnnotation.getBoundingPoly(), imageWidth, imageHeight);
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
   * We then normalize it to a [0, 1] bound so that comparisons are size agnostic.
   * This rounding lets us more accurately measure text on the same "line" while accounting for character differences.
   */
  private ImageText determineImageTextPosition(BoundingPoly boundingPoly, int imageWidth, int imageHeight) {

    var result = new ImageText();

    var topLeft = boundingPoly.getVertices(0);

    result.setX(normalizeValue(topLeft.getX(), imageWidth));
    result.setY(normalizeValue(topLeft.getY(), imageHeight));

    return result;
  }

  /**
   * Adjust a value to be between [0, 1] when compared to a total.
   */
  private double normalizeValue(int value, int total) {
    var result = new BigDecimal(value);
    result = result.setScale(-1, RoundingMode.HALF_UP); // Round to nearest 10
    result = result.divide(new BigDecimal(total), 5, RoundingMode.HALF_UP); // Round to 5th decimal place
    return result.doubleValue();
  }

  /**
   * Remove excess whitespace and sort result.
   *
   * Part of our OCR involves nudging Y values and adjusting text so this is run several times.
   */
  private void imageTextCleanup(List<ImageText> imageTexts) {

    imageTexts.forEach(imageText -> {
      var value = imageText.getValue();
      value = RegExUtils.replaceAll(value, "\n", " ");
      value = StringUtils.trim(value);
      imageText.setValue(value);
    });

    imageTexts.sort(imageTextNaturalOrderComparator());
  }

  /**
   * We read things top-to-bottom and left-to-right.
   */
  private Comparator<ImageText> imageTextNaturalOrderComparator() {
    return Comparator
      .comparingDouble(ImageText::getY)
      .thenComparingDouble(ImageText::getX);
  }

  /**
   * Nudges a second image text y-value to the same line as its predecessor if they are close enough.
   *
   * Cleans up a lot of not-quite-same-line issues.
   */
  private void setSimilarImageTextHeightsToEqualValues(List<ImageText> imageTexts) {
    for (int i = 0; i < imageTexts.size() - 1; i++) {

      var currentImageText = imageTexts.get(i);
      var nextImageText = imageTexts.get(i + 1);

      if (imageTextsOnSameLine(currentImageText, nextImageText)) {
        nextImageText.setY(currentImageText.getY());
      }
    }
  }

  /**
   * Image texts on the same line should be combined to form meaningful phrases.
   *
   * Takes special care for image texts that occur in exercise blocks.
   * Y-height alone is not enough there, it must be split down the middle as well.
   */
  private void combineImageTextsThatShouldBeGrouped(List<ImageText> imageTexts) {

    var iterator = imageTexts.listIterator();
    while (iterator.hasNext()) {

      var currentImageText = iterator.next();

      if (iterator.hasNext()) {

        var nextImageText = iterator.next();

        if (imageTextsCanBeCombined(currentImageText, nextImageText)) {
          var combinedValue = STR."\{currentImageText.getValue()} \{nextImageText.getValue()}";
          currentImageText.setValue(combinedValue);
          iterator.remove();
        }

        iterator.previous();
      }
    }
  }

  /**
   * Comparing two image text y-values to see if they are close enough to be on the same line and half of image.
   */
  private boolean imageTextsOnSameLine(ImageText a, ImageText b) {
    return Precision.equals(a.getY(), b.getY(), Y_HEIGHT_DIFFERENCE_CONSIDERED_FOR_SAME_LINE);
  }

  /**
   * Comparing two image text y-values to see if they are close enough to be on the same line and half of image.
   */
  private boolean imageTextsCanBeCombined(ImageText a, ImageText b) {
    return imageTextsOnSameLine(a, b) && Math.round(a.getX()) == Math.round(b.getX());
  }

}
