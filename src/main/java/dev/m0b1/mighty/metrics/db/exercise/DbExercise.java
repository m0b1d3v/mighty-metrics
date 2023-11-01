package dev.m0b1.mighty.metrics.db.exercise;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DbExercise {

  public static final String TABLE = "exercise";

  public static final String COLUMN_ID = "id";
  public static final String COLUMN_ID_SCORECARD = "id_scorecard";
  public static final String COLUMN_ID_SCORE = "id_score";
  public static final String COLUMN_UUID = "uuid";
  public static final String COLUMN_VALUE = "value";

  @JsonIgnore
  private Long id;

  private Long idScore;

  @Size(max = 256, message = "Exercise input cannot be longer than ${max} characters")
  private String exercise;

}
