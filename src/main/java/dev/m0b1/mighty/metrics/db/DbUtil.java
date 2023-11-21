package dev.m0b1.mighty.metrics.db;

import jakarta.annotation.Nonnull;
import lombok.experimental.UtilityClass;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Predicate;

/**
 * Common shared logic for database repository interactions.
 */
@UtilityClass
public final class DbUtil {

  public static List<String> resultSetColumns(ResultSet resultSet) throws SQLException {

    var result = new ArrayList<String>();

    var metadata = resultSet.getMetaData();
    var columnCount = metadata.getColumnCount();

    // Result set indexes start at 1
    for (int i = 1; i <= columnCount; i++) {
      result.add(metadata.getColumnName(i));
    }

    return result;
  }

  public static <T> T safeMap(
    ResultSet resultSet,
    List<String> knownColumns,
    String column,
    Class<T> type
  ) throws SQLException {

    T result = null;

    if (knownColumns.contains(column) && resultSet.getObject(column) != null) {
      result = resultSet.getObject(column, type);
    }

    return result;
  }

  /**
   * Generates an INSERT statement that swaps to an UPDATE if the primary key given already exists.
   *
   * @param inputMap Column names mapped to their values.
   * @param conflictUpdateFilter If running an UPDATE instead of INSERT, only update columns by names that match this.
   * @param table What table to run the statement against.
   * @param idColumn What column to use when determining primary key collision.
   */
  public static void upsert(
    JdbcTemplate jdbcTemplate,
    LinkedHashMap<String, Object> inputMap,
    @Nonnull Predicate<String> conflictUpdateFilter,
    String table,
    String idColumn
  ) {

    var columns = inputMap.keySet();
    var values = inputMap.values().toArray();
    var insertPlaceholders = Collections.nCopies(inputMap.size(), "?");

    var conflictUpdates = columns.stream()
      .filter(conflictUpdateFilter)
      .map(k -> STR."\{k} = excluded.\{k}")
      .toList();

    var sql = STR."""
      INSERT INTO \{table} (
        \{String.join(",\n  ", columns)}
      )
      VALUES (\{String.join(", ", insertPlaceholders)})
      ON CONFLICT (\{idColumn})
      DO UPDATE SET
        \{String.join(",\n  ", conflictUpdates)}
      """;

    jdbcTemplate.update(sql, values);
  }

}
