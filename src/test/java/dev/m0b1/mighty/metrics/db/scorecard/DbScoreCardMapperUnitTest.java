package dev.m0b1.mighty.metrics.db.scorecard;

import dev.m0b1.mighty.metrics.UnitTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class DbScoreCardMapperUnitTest extends UnitTestBase {

  @InjectMocks
  private DbScoreCardMapper mapper;

  @Mock
  private ResultSet resultSet;

  private final UUID uuid = UUID.randomUUID();

  private final LocalDateTime localDateTime = LocalDateTime.now();

  @BeforeEach
  public void beforeEach() throws SQLException {
    when(resultSet.getObject(any(String.class))).thenReturn(1, 2, 3, 4, 5);
    when(resultSet.getObject(any(String.class), eq(Integer.class))).thenReturn(1, 2, 3, 4, 5);
    when(resultSet.getString(DbScoreCard.COLUMN_UUID)).thenReturn(uuid.toString());
  }

  @Test
  void extractData() throws SQLException {

    when(resultSet.getString(DbScoreCard.COLUMN_LOCAL_DATE_TIME)).thenReturn(localDateTime.toString());

    var result = runMapper();

    assertCommonFields(result);
    assertEquals(localDateTime, result.getLocalDateTime());
  }

  @Test
  void extractDataWithoutLocalDateTime() throws SQLException {

    when(resultSet.getString(DbScoreCard.COLUMN_LOCAL_DATE_TIME)).thenReturn(null);

    var result = runMapper();

    assertCommonFields(result);
    assertNull(result.getLocalDateTime());
  }

  private DbScoreCard runMapper() throws SQLException {
    return mapper.mapRow(resultSet, 0);
  }

  private void assertCommonFields(DbScoreCard result) {
    assertNotNull(result);
    assertEquals(1, result.getIdCoach());
    assertEquals(2, result.getIdScoreGroup());
    assertEquals(3, result.getIdScorePersonal());
    assertEquals(uuid, result.getUuid());
    assertEquals(4, result.getWorkoutIntensity());
    assertEquals(5, result.getMighteriumCollected());
  }

}
