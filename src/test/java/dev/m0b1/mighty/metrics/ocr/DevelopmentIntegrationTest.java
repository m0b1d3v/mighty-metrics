package dev.m0b1.mighty.metrics.ocr;

import com.google.cloud.vision.v1.ImageAnnotatorClient;
import dev.m0b1.mighty.metrics.UnitTestBase;
import dev.m0b1.mighty.metrics.ocr.GoogleCloudVisionApi;
import dev.m0b1.mighty.metrics.ocr.ImageParser;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Spy;

@Slf4j
class DevelopmentIntegrationTest extends UnitTestBase {

  @InjectMocks
  private ImageParser imageParser;

  @Spy
  private GoogleCloudVisionApi googleCloudVisionApi;

  @ParameterizedTest
  @ValueSource(strings = {
    // "VRChat_2023-07-24_19-50-11.045_2560x1440.png",
    // "VRChat_2023-07-27_20-55-53.227_2560x1440.png",
          "images/VRChat_2023-09-01_20-49-31.549_2560x1440.png",
    // "VRChat_2023-09-09_15-52-58.389_2560x1440.png",
  })
  @Disabled
  void development(String inputFileName) {

    var classLoader = getClass().getClassLoader();

    try (
      var client = ImageAnnotatorClient.create();
      var inputStream = classLoader.getResourceAsStream(inputFileName)
    ) {

      if (inputStream == null) {
        return;
      }

      var annotatedImageResponse = googleCloudVisionApi.process(client, inputStream);
      var results = imageParser.read(annotatedImageResponse);

      var breakpoint = 1;

    } catch (Exception e) {
      log.error("Unexpected exception during test", e);
    }
  }

}
