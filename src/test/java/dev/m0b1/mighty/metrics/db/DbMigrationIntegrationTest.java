package dev.m0b1.mighty.metrics.db;

import dev.m0b1.mighty.metrics.IntegrationTestBase;
import dev.m0b1.mighty.metrics.db.coach.DbCoach;
import dev.m0b1.mighty.metrics.db.coach.DbCoachRepository;
import dev.m0b1.mighty.metrics.db.score.DbScore;
import dev.m0b1.mighty.metrics.db.score.DbScoreRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DbMigrationIntegrationTest extends IntegrationTestBase {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Autowired
  private DbCoachRepository dbCoachRepository;

  @Autowired
  private DbScoreRepository dbScoreRepository;

  @Test
  void version() {
    var result = jdbcTemplate.queryForObject("PRAGMA user_version;", Integer.class);
    assertEquals(8, result);
  }

  @ParameterizedTest
  @MethodSource
  void schema(String table, String expectedSchema) {

    var sql = "SELECT sql FROM sqlite_master WHERE type='table' AND name=?;";
    var result = jdbcTemplate.queryForObject(sql, String.class, table);

    assertEquals(expectedSchema, result);
  }

  @ParameterizedTest
  @ValueSource(strings = {"member", "scorecard"})
  void index(String table) {

    var indexName = String.format("index_%s_uuid", table);

    var sql = "SELECT sql FROM sqlite_master WHERE type='index' AND name=? AND tbl_name=?;";
    var result = jdbcTemplate.queryForObject(sql, String.class, indexName, table);

    var expected = String.format("CREATE UNIQUE INDEX %s ON %s (uuid)", indexName, table);

    assertEquals(expected, result);
  }

  @Test
  void coaches() {

    var result = dbCoachRepository.read();

    assertEquals(3, result.size());
    assertCoach(result.get(0), 2, "AyumiKoi");
    assertCoach(result.get(1), 3, "Bossun");
    assertCoach(result.get(2), 1, "Kugo");
  }

  @Test
  void scores() {

    var result = dbScoreRepository.read();

    assertEquals(6, result.size());
    assertScore(result.get(0), 1, "S+");
    assertScore(result.get(1), 2, "S");
    assertScore(result.get(2), 3, "A");
    assertScore(result.get(3), 4, "B");
    assertScore(result.get(4), 5, "C");
    assertScore(result.get(5), 6, "D");
  }

  private void assertCoach(DbCoach dbCoach, int expectedId, String expectedName) {
    assertEquals(expectedId, dbCoach.getId());
    assertEquals(expectedName, dbCoach.getName());
  }

  private void assertScore(DbScore dbScore, int expectedId, String expectedValue) {
    assertEquals(expectedId, dbScore.getId());
    assertEquals(expectedValue, dbScore.getValue());
  }

  public static Stream<Arguments> schema() {

    var coach = Arguments.of("coach", """
      CREATE TABLE coach (
      	id INTEGER PRIMARY KEY AUTOINCREMENT,
      	name TEXT
      )"""
    );

    var member = Arguments.of("member", """
      CREATE TABLE member (
      	id INTEGER PRIMARY KEY,
      	uuid TEXT,
      	name TEXT
      )"""
    );

    var score = Arguments.of("score", """
      CREATE TABLE score (
      	id INTEGER PRIMARY KEY AUTOINCREMENT,
      	value TEXT
      )"""
    );

    var scorecard = Arguments.of("scorecard", """
      CREATE TABLE scorecard (
      	id INTEGER PRIMARY KEY AUTOINCREMENT,
      	id_member INTEGER,
      	id_coach INTEGER,
      	id_score_group INTEGER,
      	id_score_personal INTEGER,
      	uuid TEXT,
      	local_date_time TEXT,
      	workout_intensity INTEGER,
      	mighterium_collected INTEGER,
      	FOREIGN KEY (id_member) REFERENCES member (id),
      	FOREIGN KEY (id_coach) REFERENCES coach (id),
      	FOREIGN KEY (id_score_group) REFERENCES score (id),
      	FOREIGN KEY (id_score_personal) REFERENCES score (id)
      )"""
    );

    return Stream.of(coach, member, score, scorecard);
  }

}
