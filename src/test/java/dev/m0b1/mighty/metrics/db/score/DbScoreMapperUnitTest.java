package dev.m0b1.mighty.metrics.db.score;

import dev.m0b1.mighty.metrics.UnitTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

class DbScoreMapperUnitTest extends UnitTestBase {

  @InjectMocks
  private DbScoreMapper mapper;

  @Mock
  private ResultSet resultSet;

  @BeforeEach
  public void beforeEach() throws SQLException {
    when(resultSet.getObject(DbScore.COLUMN_ID)).thenReturn(1);
    when(resultSet.getObject(DbScore.COLUMN_ID, Integer.class)).thenReturn(1);
    when(resultSet.getString(DbScore.COLUMN_VALUE)).thenReturn("a");
  }

  @Test
  void mapRow() throws SQLException {

    var result = mapper.mapRow(resultSet, 0);

    assertNotNull(result);
    assertEquals(1, result.getId());
    assertEquals("a", result.getValue());
  }

}
