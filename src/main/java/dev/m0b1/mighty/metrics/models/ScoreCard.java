package dev.m0b1.mighty.metrics.models;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class ScoreCard {

  private Long id;

  private Long idCoach;

  private Long idGroupAverageScore;

  private Long idPersonalAverageScore;

  @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime localDateTime;

  @Min(value = 0, message = "Workout intensity must be at least ${value}")
  @Max(value = 100, message = "Workout intensity cannot be higher than ${value}")
  private Integer workoutIntensity;

  @Min(value = 0, message = "Mighterium collected must be at least ${value}")
  @Max(value = 100_000, message = "Mighterium collected cannot be higher than the core can count")
  private Integer mighteriumCollected;

  @Size(max = 50, message = "Number of exercises cannot be higher than ${value}")
  private List<Exercise> exercises = new ArrayList<>();

}
