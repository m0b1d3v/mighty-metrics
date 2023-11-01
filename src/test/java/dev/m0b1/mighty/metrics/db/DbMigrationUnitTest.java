package dev.m0b1.mighty.metrics.db;

import dev.m0b1.mighty.metrics.UnitTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DbMigrationUnitTest extends UnitTestBase {

  @InjectMocks
  private DbMigration dbMigration;

  @Mock
  private JdbcTemplate jdbcTemplate;

  @Captor
  private ArgumentCaptor<String> stringArgumentCaptor;

  @BeforeEach
  public void beforeEach() {
    mockSchemaVersion(0);
  }

  @Test
  void postConstructWal() {
    dbMigration.postConstruct();
    verify(jdbcTemplate).execute("PRAGMA journal_mode = WAL;");
  }

  @Test
  void schemaVersionControlsMigrationsRun() {
    mockSchemaVersion(Integer.MAX_VALUE);
    runMigrations();
    verify(jdbcTemplate).queryForObject("PRAGMA user_version;", Integer.class);
    verify(jdbcTemplate, never()).execute(any(String.class));
  }

  @Test
  void migrationExceptionsCaught() {

    doThrow(new RuntimeException("testing")).when(jdbcTemplate).execute(any(String.class));

    assertThrows(RuntimeException.class, this::runMigrations);
  }

  @Test
  void migrationsRunAndUpdateSchema() {

    runMigrations();

    verify(jdbcTemplate, atLeast(2)).execute(stringArgumentCaptor.capture());

    var sql = stringArgumentCaptor.getAllValues();

    assertTrue(sql.get(0).startsWith("CREATE TABLE IF NOT EXISTS coach"));
    assertEquals("PRAGMA user_version = 1;", sql.get(1));
    assertTrue(sql.get(2).startsWith("INSERT OR IGNORE INTO coach"));
    assertEquals("PRAGMA user_version = 2;", sql.get(3));
  }

  private void mockSchemaVersion(int value) {
    when(jdbcTemplate.queryForObject("PRAGMA user_version;", Integer.class)).thenReturn(value);
  }

  private void runMigrations() {
    dbMigration.afterSingletonsInstantiated();
  }

}
