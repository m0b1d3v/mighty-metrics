package dev.m0b1.mighty.metrics.db.scorecard;

import dev.m0b1.mighty.metrics.UnitTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

class DbScoreCardRepositoryUnitTest extends UnitTestBase {

  @InjectMocks
  private DbScoreCardRepository repository;

  @Mock
  private JdbcTemplate jdbcTemplate;

  private final DbScoreCard dbScoreCard = new DbScoreCard();
  private final LocalDateTime localDateTime = LocalDateTime.now();
  private final UUID uuid = UUID.randomUUID();

  @BeforeEach
  public void beforeEach() {
    dbScoreCard.setIdMember(1L);
    dbScoreCard.setIdCoach(2);
    dbScoreCard.setIdScoreGroup(3);
    dbScoreCard.setIdScorePersonal(4);
    dbScoreCard.setUuid(uuid);
    dbScoreCard.setLocalDateTime(localDateTime);
    dbScoreCard.setWorkoutIntensity(5);
    dbScoreCard.setMighteriumCollected(6);
  }

  @Test
  void read() {

    repository.read(uuid);

    var expected = "SELECT " +
        "id_coach, " +
        "id_score_group, " +
        "id_score_personal, " +
        "uuid, " +
        "local_date_time, " +
        "workout_intensity, " +
        "mighterium_collected " +
      "FROM scorecard " +
      "WHERE uuid = ?";

    verify(jdbcTemplate).queryForObject(eq(expected), any(DbScoreCardMapper.class), eq(uuid));
  }

  @Test
  void upsert() {

    repository.upsert(dbScoreCard);

    var expectedSql = getUpsertSql();

    verify(jdbcTemplate).update(expectedSql, 1L, 2, 3, 4, uuid, localDateTime, 5, 6);
  }

  @Test
  void upsertWithoutUuid() {

    dbScoreCard.setUuid(null);

    repository.upsert(dbScoreCard);

    var expectedSql = getUpsertSql();

    verify(jdbcTemplate).update(
      eq(expectedSql),
      eq(1L),
      eq(2),
      eq(3),
      eq(4),
      any(UUID.class),
      eq(localDateTime),
      eq(5),
      eq(6)
    );
  }

  private String getUpsertSql() {
    return "INSERT INTO scorecard (" +
      "id_member, " +
      "id_coach, " +
      "id_score_group, " +
      "id_score_personal, " +
      "uuid, " +
      "local_date_time, " +
      "workout_intensity, " +
      "mighterium_collected" +
      ") " +
      "VALUES (?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT (uuid) DO UPDATE SET " +
      "id_member = excluded.id_member, " +
      "id_coach = excluded.id_coach, " +
      "id_score_group = excluded.id_score_group, " +
      "id_score_personal = excluded.id_score_personal, " +
      "uuid = excluded.uuid, " +
      "local_date_time = excluded.local_date_time, " +
      "workout_intensity = excluded.workout_intensity, " +
      "mighterium_collected = excluded.mighterium_collected";
  }

}
