package dev.m0b1.mighty.metrics.db.score;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class DbScoreRepository {

  private static final DbScoreMapper MAPPER = new DbScoreMapper();

  private final JdbcTemplate jdbcTemplate;

  public List<DbScore> read() {

    var sql = STR."""
      SELECT
        \{DbScore.COLUMN_ID},
        \{DbScore.COLUMN_VALUE}
      FROM \{DbScore.TABLE}
      ORDER BY \{DbScore.COLUMN_ID}
      """;

    return jdbcTemplate.query(sql, MAPPER);
  }

}
