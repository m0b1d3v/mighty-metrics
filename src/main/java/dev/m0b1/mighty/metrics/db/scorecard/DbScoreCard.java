package dev.m0b1.mighty.metrics.db.scorecard;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Data
public class DbScoreCard {

  public static final String TABLE = "scorecard";

  public static final String COLUMN_ID = "id";
  public static final String COLUMN_DELETED = "deleted";
  public static final String COLUMN_ID_MEMBER = "id_member";
  public static final String COLUMN_ID_COACH = "id_coach";
  public static final String COLUMN_ID_SCORE_GROUP = "id_score_group";
  public static final String COLUMN_ID_SCORE_PERSONAL = "id_score_personal";
  public static final String COLUMN_UUID = "uuid";
  public static final String COLUMN_DATE = "date";
  public static final String COLUMN_TIME = "time";
  public static final String COLUMN_WORKOUT_INTENSITY = "workout_intensity";
  public static final String COLUMN_MIGHTERIUM_COLLECTED = "mighterium_collected";
  public static final String COLUMN_EXERCISES = "exercises";
  public static final String COLUMN_IMAGE_TITLE = "image_title";
  public static final String COLUMN_IMAGE_BYTES = "image_bytes";

  @JsonIgnore
  private Long id;

  @JsonIgnore
  private Long idMember;

  @Min(value = 1, message = "Invalid coach detected")
  @Max(value = 3, message = "Invalid coach detected")
  private Integer idCoach;

  @Min(value = 1, message = "Invalid score detected")
  @Max(value = 6, message = "Invalid score detected")
  private Integer idScoreGroup;

  @Min(value = 1, message = "Invalid score detected")
  @Max(value = 6, message = "Invalid score detected")
  private Integer idScorePersonal;

  private UUID uuid;

  @DateTimeFormat(pattern = "yyyy-MM-dd")
  private LocalDate date;

  @DateTimeFormat(pattern = "HH:mm")
  private LocalTime time;

  @Min(value = 0, message = "Workout intensity must be at least {value}")
  @Max(value = 100, message = "Workout intensity cannot be higher than {value}")
  private Integer workoutIntensity;

  @Min(value = 0, message = "Mighterium collected must be at least {value}")
  @Max(value = 100_000, message = "Mighterium collected cannot be higher than the core can count")
  private Integer mighteriumCollected;

  @Size(max = 50, message = "Number of exercises cannot be higher than {value}")
  @Valid
  private List<DbScoreCardExercise> exercises = new LinkedList<>();

  private String imageTitle;

  private byte[] imageBytes;

  private Double version;

}
