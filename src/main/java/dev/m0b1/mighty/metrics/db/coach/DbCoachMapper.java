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

    var columns = DbUtil.resultSetColumns(resultSet);

    result.setId(DbUtil.safeMap(resultSet, columns, DbCoach.COLUMN_ID, Integer.class));
    result.setName(DbUtil.safeMap(resultSet, columns, DbCoach.COLUMN_NAME, String.class));

    return result;
  }

}
