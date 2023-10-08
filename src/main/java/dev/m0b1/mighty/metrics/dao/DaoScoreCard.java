package dev.m0b1.mighty.metrics.dao;

import dev.m0b1.mighty.metrics.models.DatabaseTables;
import dev.m0b1.mighty.metrics.models.ScoreCard;
import dev.m0b1.mighty.metrics.models.DatabaseColumns;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class DaoScoreCard {

  private static final DaoScoreCardRowMapper DAO_SCORE_CARD_ROW_MAPPER = new DaoScoreCardRowMapper();

  private final JdbcTemplate jdbcTemplate;

  public ScoreCard read(UUID uuid) {

    var columns = String.join(", ", List.of(
      DatabaseColumns.ID_COACH,
      DatabaseColumns.ID_SCORE_GROUP,
      DatabaseColumns.ID_SCORE_PERSONAL,
      DatabaseColumns.UUID,
      DatabaseColumns.LOCAL_DATE_TIME,
      DatabaseColumns.WORKOUT_INTENSITY,
      DatabaseColumns.MIGHTERIUM_COLLECTED
    ));

    var sql = String.format("SELECT %s FROM %s WHERE uuid = ?", columns, DatabaseTables.SCORECARD);

    return jdbcTemplate.queryForObject(sql, DAO_SCORE_CARD_ROW_MAPPER, uuid);
  }

  @Transactional
  public ScoreCard upsert(ScoreCard scoreCard) {

    if (scoreCard.getUuid() == null) {
      scoreCard.setUuid(UUID.randomUUID());
    }

    var inputMap = new LinkedHashMap<String, Object>();
    inputMap.put(DatabaseColumns.ID_MEMBER,  scoreCard.getIdMember());
    inputMap.put(DatabaseColumns.ID_COACH, scoreCard.getIdCoach());
    inputMap.put(DatabaseColumns.ID_SCORE_GROUP, scoreCard.getIdScoreGroup());
    inputMap.put(DatabaseColumns.ID_SCORE_PERSONAL, scoreCard.getIdScorePersonal());
    inputMap.put(DatabaseColumns.UUID, scoreCard.getUuid());
    inputMap.put(DatabaseColumns.LOCAL_DATE_TIME, scoreCard.getLocalDateTime());
    inputMap.put(DatabaseColumns.WORKOUT_INTENSITY, scoreCard.getWorkoutIntensity());
    inputMap.put(DatabaseColumns.MIGHTERIUM_COLLECTED, scoreCard.getMighteriumCollected());

    var columns = inputMap.keySet();
    var values = inputMap.values().toArray();
    var insertPlaceholders = Collections.nCopies(inputMap.size(), "?");
    var conflictUpdates = columns.stream().map(k -> k + " = excluded." + k).toList();

    var sql = String.format(
      "INSERT INTO %s (%s) VALUES (%s) ON CONFLICT (uuid) DO UPDATE SET %s",
      DatabaseTables.SCORECARD,
      String.join(", ", columns),
      String.join(", ", insertPlaceholders),
      String.join(", ", conflictUpdates)
    );

    jdbcTemplate.update(sql, values);

    return scoreCard;
  }

}
