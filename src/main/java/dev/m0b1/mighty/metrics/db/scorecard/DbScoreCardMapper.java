package dev.m0b1.mighty.metrics.db.scorecard;

import com.fasterxml.jackson.core.type.TypeReference;
import dev.m0b1.mighty.metrics.db.DbUtil;
import dev.m0b1.mighty.metrics.util.JsonUtil;
import jakarta.annotation.Nonnull;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public class DbScoreCardMapper implements RowMapper<DbScoreCard> {

  @Override
  public DbScoreCard mapRow(@Nonnull ResultSet resultSet, int rowNumber) throws SQLException {

    var columns = DbUtil.resultSetColumns(resultSet);

    var scoreCard = new DbScoreCard();
    scoreCard.setIdCoach(DbUtil.safeMap(resultSet, columns, DbScoreCard.COLUMN_ID_COACH, Integer.class));
    scoreCard.setIdScoreGroup(DbUtil.safeMap(resultSet, columns, DbScoreCard.COLUMN_ID_SCORE_GROUP, Integer.class));
    scoreCard.setIdScorePersonal(DbUtil.safeMap(resultSet, columns, DbScoreCard.COLUMN_ID_SCORE_PERSONAL, Integer.class));
    scoreCard.setUuid(UUID.fromString(resultSet.getString(DbScoreCard.COLUMN_UUID)));
    scoreCard.setWorkoutIntensity(DbUtil.safeMap(resultSet, columns, DbScoreCard.COLUMN_WORKOUT_INTENSITY, Integer.class));
    scoreCard.setMighteriumCollected(DbUtil.safeMap(resultSet, columns, DbScoreCard.COLUMN_MIGHTERIUM_COLLECTED, Integer.class));

    var localDate = DbUtil.safeMap(resultSet, columns, DbScoreCard.COLUMN_DATE, String.class);
    if (localDate != null) {
      scoreCard.setDate(LocalDate.parse(localDate));
    }

    var localTime = DbUtil.safeMap(resultSet, columns, DbScoreCard.COLUMN_TIME, String.class);
    if (localTime != null) {
      scoreCard.setTime(LocalTime.parse(localTime));
    }

    var exercisesJson = DbUtil.safeMap(resultSet, columns, DbScoreCard.COLUMN_EXERCISES, String.class);
    var typeReference = new TypeReference<List<DbScoreCardExercise>>(){};
    var exercises = JsonUtil.read(exercisesJson, typeReference);
    if (exercises != null) {
      scoreCard.getExercises().addAll(exercises);
    }

    return scoreCard;
  }

}
