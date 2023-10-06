package dev.m0b1.mighty.metrics.models;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class Exercise {

  private Long id;

  private Long idScore;

  @Size(max = 256, message = "Exercise input cannot be longer than ${max} characters")
  private String exercise;

}
