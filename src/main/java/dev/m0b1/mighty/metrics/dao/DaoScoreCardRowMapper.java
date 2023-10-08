package dev.m0b1.mighty.metrics.dao;

import dev.m0b1.mighty.metrics.models.DatabaseColumns;
import dev.m0b1.mighty.metrics.models.ScoreCard;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.UUID;

public class DaoScoreCardRowMapper implements RowMapper<ScoreCard> {

  @Override
  public ScoreCard mapRow(ResultSet resultSet, int rowNumber) throws SQLException {

    var scoreCard = new ScoreCard();
    scoreCard.setIdCoach(resultSet.getObject(DatabaseColumns.ID_COACH, Long.class));
    scoreCard.setIdScoreGroup(resultSet.getObject(DatabaseColumns.ID_SCORE_GROUP, Long.class));
    scoreCard.setIdScorePersonal(resultSet.getObject(DatabaseColumns.ID_SCORE_PERSONAL, Long.class));
    scoreCard.setUuid(UUID.fromString(resultSet.getString(DatabaseColumns.UUID)));
    scoreCard.setWorkoutIntensity(resultSet.getObject(DatabaseColumns.WORKOUT_INTENSITY, Integer.class));
    scoreCard.setMighteriumCollected(resultSet.getObject(DatabaseColumns.MIGHTERIUM_COLLECTED, Integer.class));

    var localDateTime = resultSet.getString(DatabaseColumns.LOCAL_DATE_TIME);
    if (localDateTime != null) {
      scoreCard.setLocalDateTime(LocalDateTime.parse(localDateTime));
    }

    return scoreCard;
  }

}
