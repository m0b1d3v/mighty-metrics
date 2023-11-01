package dev.m0b1.mighty.metrics.db.coach;

import dev.m0b1.mighty.metrics.UnitTestBase;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

class DbCoachRepositoryUnitTest extends UnitTestBase {

  @InjectMocks
  private DbCoachRepository repository;

  @Mock
  private JdbcTemplate jdbcTemplate;

  @Test
  void read() {

    repository.read();

    verify(jdbcTemplate).query(
      eq("SELECT id, name FROM coach ORDER BY name"),
      any(DbCoachMapper.class)
    );
  }

}
