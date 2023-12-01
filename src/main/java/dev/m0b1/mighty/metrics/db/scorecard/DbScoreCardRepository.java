package dev.m0b1.mighty.metrics.db.scorecard;

import dev.m0b1.mighty.metrics.db.DbUtil;
import dev.m0b1.mighty.metrics.json.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import static java.lang.StringTemplate.STR;

@Repository
@RequiredArgsConstructor
@Slf4j
public class DbScoreCardRepository {

  private static final DbScoreCardMapper dbScoreCardMapper = new DbScoreCardMapper();

  private final JdbcTemplate jdbcTemplate;

  public List<DbScoreCard> readAll(Long idMember) {

    var sql = STR."""
      SELECT
        \{DbScoreCard.COLUMN_ID_SCORE_GROUP},
        \{DbScoreCard.COLUMN_ID_SCORE_PERSONAL},
        \{DbScoreCard.COLUMN_UUID},
        \{DbScoreCard.COLUMN_DATE},
        \{DbScoreCard.COLUMN_TIME},
        \{DbScoreCard.COLUMN_WORKOUT_INTENSITY},
        \{DbScoreCard.COLUMN_MIGHTERIUM_COLLECTED},
        \{DbScoreCard.COLUMN_IMAGE_TITLE},
        \{DbScoreCard.COLUMN_IMAGE_BYTES}
      FROM \{DbScoreCard.TABLE}
      WHERE \{DbScoreCard.COLUMN_ID_MEMBER} = ?
        AND \{DbScoreCard.COLUMN_DELETED} IS NOT TRUE
      ORDER BY \{DbScoreCard.COLUMN_DATE} DESC, \{DbScoreCard.COLUMN_TIME} DESC, \{DbScoreCard.COLUMN_ID} DESC
      """;

    return jdbcTemplate.query(sql, dbScoreCardMapper, idMember);
  }

  public DbScoreCard readData(UUID uuid) {

    var sql = STR."""
      SELECT
        \{DbScoreCard.COLUMN_ID_COACH},
        \{DbScoreCard.COLUMN_ID_SCORE_GROUP},
        \{DbScoreCard.COLUMN_ID_SCORE_PERSONAL},
        \{DbScoreCard.COLUMN_UUID},
        \{DbScoreCard.COLUMN_DATE},
        \{DbScoreCard.COLUMN_TIME},
        \{DbScoreCard.COLUMN_WORKOUT_INTENSITY},
        \{DbScoreCard.COLUMN_MIGHTERIUM_COLLECTED},
        \{DbScoreCard.COLUMN_EXERCISES},
        \{DbScoreCard.COLUMN_IMAGE_TITLE}
      FROM \{DbScoreCard.TABLE}
      WHERE \{DbScoreCard.COLUMN_UUID} = ?
        AND \{DbScoreCard.COLUMN_DELETED} IS NOT TRUE
      """;

    return jdbcTemplate.queryForObject(sql, dbScoreCardMapper, uuid);
  }

  public DbScoreCard readImage(UUID uuid) {

    var sql = STR."""
      SELECT
        \{DbScoreCard.COLUMN_IMAGE_BYTES}
      FROM \{DbScoreCard.TABLE}
      WHERE \{DbScoreCard.COLUMN_UUID} = ?
        AND \{DbScoreCard.COLUMN_DELETED} IS NOT TRUE
      """;

    return jdbcTemplate.queryForObject(sql, dbScoreCardMapper, uuid);
  }

  public DbScoreCard upsert(DbScoreCard dbScoreCard, boolean imageDataAdded) {

    if (dbScoreCard.getUuid() == null) {
      dbScoreCard.setUuid(UUID.randomUUID());
    }

    var exercisesJson = JsonUtil.write(dbScoreCard.getExercises(), "[]");

    var inputMap = new LinkedHashMap<String, Object>();
    inputMap.put(DbScoreCard.COLUMN_ID_MEMBER, dbScoreCard.getIdMember());
    inputMap.put(DbScoreCard.COLUMN_ID_COACH, dbScoreCard.getIdCoach());
    inputMap.put(DbScoreCard.COLUMN_ID_SCORE_GROUP, dbScoreCard.getIdScoreGroup());
    inputMap.put(DbScoreCard.COLUMN_ID_SCORE_PERSONAL, dbScoreCard.getIdScorePersonal());
    inputMap.put(DbScoreCard.COLUMN_UUID, dbScoreCard.getUuid());
    inputMap.put(DbScoreCard.COLUMN_DATE, dbScoreCard.getDate());
    inputMap.put(DbScoreCard.COLUMN_TIME, dbScoreCard.getTime());
    inputMap.put(DbScoreCard.COLUMN_WORKOUT_INTENSITY, dbScoreCard.getWorkoutIntensity());
    inputMap.put(DbScoreCard.COLUMN_MIGHTERIUM_COLLECTED, dbScoreCard.getMighteriumCollected());
    inputMap.put(DbScoreCard.COLUMN_EXERCISES, exercisesJson);

    if (imageDataAdded) {
      inputMap.put(DbScoreCard.COLUMN_IMAGE_TITLE, dbScoreCard.getImageTitle());
      inputMap.put(DbScoreCard.COLUMN_IMAGE_BYTES, dbScoreCard.getImageBytes());
    }

    DbUtil.upsert(
      jdbcTemplate,
      inputMap,
      column -> true, // All columns can be updated if there is a conflict
      DbScoreCard.TABLE,
      DbScoreCard.COLUMN_UUID
    );

    return dbScoreCard;
  }

  public void delete(DbScoreCard dbScoreCard) {

    var inputMap = new LinkedHashMap<String, Object>();
    inputMap.put(DbScoreCard.COLUMN_UUID, dbScoreCard.getUuid());
    inputMap.put(DbScoreCard.COLUMN_DELETED, true);

    DbUtil.upsert(jdbcTemplate, inputMap, column -> true, DbScoreCard.TABLE, DbScoreCard.COLUMN_UUID);
  }

}
