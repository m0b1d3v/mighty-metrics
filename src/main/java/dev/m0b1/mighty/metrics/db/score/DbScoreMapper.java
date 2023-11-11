package dev.m0b1.mighty.metrics.db.score;

import dev.m0b1.mighty.metrics.db.DbUtil;
import jakarta.annotation.Nonnull;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DbScoreMapper implements RowMapper<DbScore> {

  @Override
  public DbScore mapRow(@Nonnull ResultSet resultSet, int rowNumber) throws SQLException {

    var result = new DbScore();

    result.setId(DbUtil.safeMap(resultSet, DbScore.COLUMN_ID, Integer.class));
    result.setValue(resultSet.getString(DbScore.COLUMN_VALUE));

    return result;
  }

}
