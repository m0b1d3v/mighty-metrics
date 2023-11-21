package dev.m0b1.mighty.metrics.db.score;

import dev.m0b1.mighty.metrics.UnitTestBase;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

class DbScoreRepositoryUnitTest extends UnitTestBase {

  @InjectMocks
  private DbScoreRepository repository;

  @Mock
  private JdbcTemplate jdbcTemplate;

  @Test
  void read() {

    repository.read();

    var expectedSql = """
      SELECT
        id,
        value
      FROM score
      ORDER BY id
      """;

    verify(jdbcTemplate).query(
      eq(expectedSql),
      any(DbScoreMapper.class)
    );
  }

}
