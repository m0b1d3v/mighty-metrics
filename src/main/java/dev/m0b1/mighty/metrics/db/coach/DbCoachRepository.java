package dev.m0b1.mighty.metrics.db.coach;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class DbCoachRepository {

  private static final DbCoachMapper MAPPER = new DbCoachMapper();

  private final JdbcTemplate jdbcTemplate;

  public List<DbCoach> read() {

    var sql = STR."""
      SELECT
        \{DbCoach.COLUMN_ID},
        \{DbCoach.COLUMN_NAME}
      FROM \{DbCoach.TABLE}
      ORDER BY \{DbCoach.COLUMN_NAME}
      """;

    return jdbcTemplate.query(sql, MAPPER);
  }

}
