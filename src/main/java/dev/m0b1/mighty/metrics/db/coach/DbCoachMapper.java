package dev.m0b1.mighty.metrics.db.coach;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DbCoachMapper implements RowMapper<DbCoach> {

  @Override
  public DbCoach mapRow(ResultSet resultSet, int rowNumber) throws SQLException {

    var result = new DbCoach();

    result.setId(resultSet.getLong(DbCoach.COLUMN_ID));
    result.setName(resultSet.getString(DbCoach.COLUMN_NAME));

    return result;
  }

}
