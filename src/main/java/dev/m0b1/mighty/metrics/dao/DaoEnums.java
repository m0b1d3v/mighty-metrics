package dev.m0b1.mighty.metrics.dao;

import dev.m0b1.mighty.metrics.models.DatabaseColumns;
import dev.m0b1.mighty.metrics.models.DatabaseTables;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class DaoEnums {

  private final JdbcTemplate jdbcTemplate;

  public Map<Long, String> coaches() {
    return queryForMap(DatabaseTables.COACH, DatabaseColumns.NAME, DatabaseColumns.NAME);
  }

  public Map<Long, String> scores() {
    return queryForMap(DatabaseTables.SCORE, DatabaseColumns.VALUE, DatabaseColumns.ID);
  }

  private Map<Long, String> queryForMap(String table, String column, String orderBy) {

    var sql = String.format("SELECT %s, %s FROM %s ORDER BY %s", DatabaseColumns.ID, column, table, orderBy);

    var result = new LinkedHashMap<Long, String>();

    jdbcTemplate.query(sql, (resultSet) -> {
      var id = resultSet.getLong(DatabaseColumns.ID);
      var name = resultSet.getString(column);
      result.put(id, name);
    });

    return result;
  }

}
