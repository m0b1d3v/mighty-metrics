package dev.m0b1.mighty.metrics.models;

import dev.m0b1.mighty.metrics.enums.EnumScore;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class Exercise {

  @Size(max = 256, message = "Exercise input cannot be longer than ${max} characters")
  private String exercise;

  private EnumScore score;

}
