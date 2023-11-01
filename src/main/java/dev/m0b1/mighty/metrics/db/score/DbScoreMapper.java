package dev.m0b1.mighty.metrics.db.score;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DbScoreMapper implements RowMapper<DbScore> {

  @Override
  public DbScore mapRow(ResultSet resultSet, int rowNumber) throws SQLException {

    var result = new DbScore();

    result.setId(resultSet.getLong(DbScore.COLUMN_ID));
    result.setValue(resultSet.getString(DbScore.COLUMN_VALUE));

    return result;
  }

}
