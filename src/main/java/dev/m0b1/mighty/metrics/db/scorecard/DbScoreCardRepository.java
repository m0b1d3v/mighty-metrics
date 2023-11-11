package dev.m0b1.mighty.metrics.db.scorecard;

import dev.m0b1.mighty.metrics.db.DbUtil;
import dev.m0b1.mighty.metrics.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Slf4j
public class DbScoreCardRepository {

  private static final DbScoreCardMapper MAPPER = new DbScoreCardMapper();

  private final JdbcTemplate jdbcTemplate;

  public DbScoreCard read(UUID uuid) {

    var columns = String.join(", ", List.of(
      DbScoreCard.COLUMN_ID_COACH,
      DbScoreCard.COLUMN_ID_SCORE_GROUP,
      DbScoreCard.COLUMN_ID_SCORE_PERSONAL,
      DbScoreCard.COLUMN_UUID,
      DbScoreCard.COLUMN_LOCAL_DATE_TIME,
      DbScoreCard.COLUMN_WORKOUT_INTENSITY,
      DbScoreCard.COLUMN_MIGHTERIUM_COLLECTED,
      DbScoreCard.COLUMN_EXERCISES
    ));

    var sql = String.format("SELECT %s FROM %s WHERE uuid = ?", columns, DbScoreCard.TABLE);

    return jdbcTemplate.queryForObject(sql, MAPPER, uuid);
  }

  public DbScoreCard upsert(DbScoreCard dbScoreCard) {

    if (dbScoreCard.getUuid() == null) {
      dbScoreCard.setUuid(UUID.randomUUID());
    }

    var exercisesJson = JsonUtil.write(dbScoreCard.getExercises(), "[]");

    var inputMap = new LinkedHashMap<String, Object>();
    inputMap.put(DbScoreCard.COLUMN_ID_MEMBER,  dbScoreCard.getIdMember());
    inputMap.put(DbScoreCard.COLUMN_ID_COACH, dbScoreCard.getIdCoach());
    inputMap.put(DbScoreCard.COLUMN_ID_SCORE_GROUP, dbScoreCard.getIdScoreGroup());
    inputMap.put(DbScoreCard.COLUMN_ID_SCORE_PERSONAL, dbScoreCard.getIdScorePersonal());
    inputMap.put(DbScoreCard.COLUMN_UUID, dbScoreCard.getUuid());
    inputMap.put(DbScoreCard.COLUMN_LOCAL_DATE_TIME, dbScoreCard.getLocalDateTime());
    inputMap.put(DbScoreCard.COLUMN_WORKOUT_INTENSITY, dbScoreCard.getWorkoutIntensity());
    inputMap.put(DbScoreCard.COLUMN_MIGHTERIUM_COLLECTED, dbScoreCard.getMighteriumCollected());
    inputMap.put(DbScoreCard.COLUMN_EXERCISES, exercisesJson);

    DbUtil.upsert(
      jdbcTemplate,
      inputMap,
      column -> true, // All columns can be updated if there is a conflict
      DbScoreCard.TABLE,
      DbScoreCard.COLUMN_UUID
    );

    return dbScoreCard;
  }

}
