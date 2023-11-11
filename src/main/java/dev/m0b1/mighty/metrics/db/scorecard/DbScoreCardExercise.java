package dev.m0b1.mighty.metrics.db.scorecard;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DbScoreCardExercise {

  @Min(value = 1, message = "Invalid score detected")
  @Max(value = 6, message = "Invalid score detected")
  private Integer idScore;

  @Size(max = 10, message = "Exercise input cannot be longer than {max}")
  private String exercise;

}
