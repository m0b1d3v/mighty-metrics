package dev.m0b1.mighty.metrics.db.coach;

import dev.m0b1.mighty.metrics.db.DbUtil;
import jakarta.annotation.Nonnull;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DbCoachMapper implements RowMapper<DbCoach> {

  @Override
  public DbCoach mapRow(@Nonnull ResultSet resultSet, int rowNumber) throws SQLException {

    var result = new DbCoach();

    result.setId(DbUtil.safeMap(resultSet, DbCoach.COLUMN_ID, Integer.class));
    result.setName(resultSet.getString(DbCoach.COLUMN_NAME));

    return result;
  }

}
