package dev.m0b1.mighty.metrics.db.scorecard;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.m0b1.mighty.metrics.db.exercise.DbExercise;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class DbScoreCard {

  public static final String TABLE = "scorecard";

  public static final String COLUMN_ID = "id";
  public static final String COLUMN_ID_MEMBER = "id_member";
  public static final String COLUMN_ID_COACH = "id_coach";
  public static final String COLUMN_ID_SCORE_GROUP = "id_score_group";
  public static final String COLUMN_ID_SCORE_PERSONAL = "id_score_personal";
  public static final String COLUMN_UUID = "uuid";
  public static final String COLUMN_LOCAL_DATE_TIME = "local_date_time";
  public static final String COLUMN_WORKOUT_INTENSITY = "workout_intensity";
  public static final String COLUMN_MIGHTERIUM_COLLECTED = "mighterium_collected";

  @JsonIgnore
  private Long id;

  @JsonIgnore
  private Long idMember;

  private Integer idCoach;

  private Integer idScoreGroup;

  private Integer idScorePersonal;

  private UUID uuid;

  @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
  private LocalDateTime localDateTime;

  @Min(value = 0, message = "Workout intensity must be at least ${value}")
  @Max(value = 100, message = "Workout intensity cannot be higher than ${value}")
  private Integer workoutIntensity;

  @Min(value = 0, message = "Mighterium collected must be at least ${value}")
  @Max(value = 100_000, message = "Mighterium collected cannot be higher than the core can count")
  private Integer mighteriumCollected;

  @Size(max = 50, message = "Number of exercises cannot be higher than ${value}")
  private List<DbExercise> exercises = new ArrayList<>();

}
