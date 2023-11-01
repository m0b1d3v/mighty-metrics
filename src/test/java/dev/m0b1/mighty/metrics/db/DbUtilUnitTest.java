package dev.m0b1.mighty.metrics.db;

import dev.m0b1.mighty.metrics.UnitTestBase;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.LinkedHashMap;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.verify;

class DbUtilUnitTest extends UnitTestBase {

  @Mock
  private JdbcTemplate jdbcTemplate;

  @Test
  void utilityClass() throws NoSuchMethodException {
    assertUtilityClass(DbUtil.class);
  }

  @Test
  void upsert() {

    var inputMap = new LinkedHashMap<String, Object>();
    inputMap.put("a", 1);
    inputMap.put("b", 2L);
    inputMap.put("c", "3");

    Predicate<String> conflictUpdateFilter = s -> s.equals("b");

    DbUtil.upsert(
      jdbcTemplate,
      inputMap,
      conflictUpdateFilter,
      "c",
      "d"
    );

    var expectedSql = "INSERT INTO c (a, b, c) VALUES (?, ?, ?) ON CONFLICT (d) DO UPDATE SET b = excluded.b";

    verify(jdbcTemplate).update(expectedSql, 1, 2L, "3");
  }

}
