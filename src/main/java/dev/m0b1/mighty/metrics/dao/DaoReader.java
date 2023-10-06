package dev.m0b1.mighty.metrics.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class DaoReader {

  private final JdbcTemplate jdbcTemplate;

  public Map<Long, String> coaches() {
    return queryForMap("coach", "name", "name");
  }

  public Map<Long, String> scores() {
    return queryForMap("score", "value", "id");
  }

  private Map<Long, String> queryForMap(String table, String column, String orderBy) {

    var sql = String.format("SELECT id, %s FROM %s ORDER BY %s", column, table, orderBy);

    var result = new LinkedHashMap<Long, String>();

    jdbcTemplate.query(sql, (resultSet) -> {
      var id = resultSet.getLong("id");
      var name = resultSet.getString(column);
      result.put(id, name);
    });

    return result;
  }

}
