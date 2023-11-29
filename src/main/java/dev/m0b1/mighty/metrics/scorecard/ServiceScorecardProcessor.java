package dev.m0b1.mighty.metrics.scorecard;

import dev.m0b1.mighty.metrics.db.scorecard.DbScoreCard;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.imaging.ImageFormats;
import org.apache.commons.imaging.Imaging;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

@RequiredArgsConstructor
@Service
@Slf4j
public class ServiceScorecardProcessor {

  private static final int SCALED_WIDTH = 720;

  private final ServiceImageOcr serviceImageOcr;
  private final ServiceImageParser serviceImageParser;

  public void run(DbScoreCard dbScoreCard, @Nonnull MultipartFile multipartFile) {

    try (var inputStream = multipartFile.getInputStream()) {

      var bufferedImage = ImageIO.read(inputStream);

      var rotatedImage = ensureCorrectScorecardRotation(bufferedImage);
      var bytesOcr = Imaging.writeImageToBytes(rotatedImage, ImageFormats.PNG);

      var scaledImage = scaleScorecardImage(rotatedImage);
      var bytesStorage = Imaging.writeImageToBytes(scaledImage, ImageFormats.PNG);

      dbScoreCard.setImageTitle(multipartFile.getOriginalFilename());
      dbScoreCard.setImageBytes(bytesStorage);

      var imageTexts = serviceImageOcr.run(bytesOcr);
      serviceImageParser.run(dbScoreCard, imageTexts);

    } catch (Exception e) {
      log.atError()
        .setMessage("Could not read uploaded file")
        .setCause(e)
        .log();
    }
  }

  private BufferedImage ensureCorrectScorecardRotation(BufferedImage source) {

    var result = source;

    var width = source.getWidth();
    var height = source.getHeight();
    if (width > height) {

      result = new BufferedImage(height, width, source.getType());

      var transformAnchor = height / 2.0;
      var affineTransform = AffineTransform.getRotateInstance(Math.toRadians(90), transformAnchor, transformAnchor);
      var affineTransformOp = new AffineTransformOp(affineTransform, AffineTransformOp.TYPE_BILINEAR);

      affineTransformOp.filter(source, result);
    }

    return result;
  }

  private BufferedImage scaleScorecardImage(BufferedImage source) {

    BufferedImage result;

    var scaledImage = source.getScaledInstance(SCALED_WIDTH, -1, Image.SCALE_DEFAULT);
    var scaledWidth = scaledImage.getWidth(null);
    var scaledHeight = scaledImage.getHeight(null);

    result = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB);
    var graphics = result.getGraphics();
    graphics.drawImage(scaledImage, 0, 0, null);
    graphics.dispose();

    return result;
  }

}
