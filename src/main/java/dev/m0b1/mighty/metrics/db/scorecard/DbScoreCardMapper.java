package dev.m0b1.mighty.metrics.db.scorecard;

import com.fasterxml.jackson.core.type.TypeReference;
import dev.m0b1.mighty.metrics.db.DbUtil;
import jakarta.annotation.Nonnull;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.UUID;

public class DbScoreCardMapper implements RowMapper<DbScoreCard> {

  @Override
  public DbScoreCard mapRow(@Nonnull ResultSet resultSet, int rowNumber) throws SQLException {

    var scoreCard = new DbScoreCard();
    scoreCard.setIdCoach(DbUtil.safeMap(resultSet, DbScoreCard.COLUMN_ID_COACH, Integer.class));
    scoreCard.setIdScoreGroup(DbUtil.safeMap(resultSet, DbScoreCard.COLUMN_ID_SCORE_GROUP, Integer.class));
    scoreCard.setIdScorePersonal(DbUtil.safeMap(resultSet, DbScoreCard.COLUMN_ID_SCORE_PERSONAL, Integer.class));
    scoreCard.setUuid(UUID.fromString(resultSet.getString(DbScoreCard.COLUMN_UUID)));
    scoreCard.setWorkoutIntensity(DbUtil.safeMap(resultSet, DbScoreCard.COLUMN_WORKOUT_INTENSITY, Integer.class));
    scoreCard.setMighteriumCollected(DbUtil.safeMap(resultSet, DbScoreCard.COLUMN_MIGHTERIUM_COLLECTED, Integer.class));

    var localDateTime = resultSet.getString(DbScoreCard.COLUMN_LOCAL_DATE_TIME);
    if (localDateTime != null) {
      scoreCard.setLocalDateTime(LocalDateTime.parse(localDateTime));
    }

    return scoreCard;
  }

}
