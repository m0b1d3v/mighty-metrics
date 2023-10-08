package dev.m0b1.mighty.metrics.dao;

import dev.m0b1.mighty.metrics.models.DatabaseColumns;
import dev.m0b1.mighty.metrics.models.DatabaseTables;
import dev.m0b1.mighty.metrics.models.OAuth2Attributes;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class DaoMember {

  private final JdbcTemplate jdbcTemplate;

  public boolean deniedScorecard(OAuth2User oAuth2User, UUID uuid) {

    var userId = oAuth2User.getAttribute(OAuth2Attributes.ID);
    var sql = String.format("SELECT COUNT(*) FROM %s WHERE uuid = ? AND id_member = ?", DatabaseTables.SCORECARD);
    var matchingColumns = jdbcTemplate.queryForObject(sql, Long.class, uuid, userId);

    return matchingColumns == null || matchingColumns < 1;
  }

  public void upsert(OAuth2User oAuth2User) {

    var uuidIfNew = UUID.randomUUID();
    var id = oAuth2User.getAttribute(OAuth2Attributes.ID);
    var name = oAuth2User.getAttribute(OAuth2Attributes.NAME);

    var inputMap = new LinkedHashMap<String, Object>();
    inputMap.put(DatabaseColumns.ID, id);
    inputMap.put(DatabaseColumns.UUID, uuidIfNew);
    inputMap.put(DatabaseColumns.NAME, name);

    var columns = inputMap.keySet();
    var values = inputMap.values().toArray();
    var insertPlaceholders = Collections.nCopies(inputMap.size(), "?");
    var conflictUpdates = columns.stream().filter(DatabaseColumns.NAME::equals).map(k -> k + " = excluded." + k).toList();

    var sql = String.format(
      "INSERT INTO %s (%s) VALUES (%s) ON CONFLICT (id) DO UPDATE SET %s",
      DatabaseTables.MEMBER,
      String.join(", ", columns),
      String.join(", ", insertPlaceholders),
      String.join(", ", conflictUpdates)
    );

    jdbcTemplate.update(sql, values);
  }

}
