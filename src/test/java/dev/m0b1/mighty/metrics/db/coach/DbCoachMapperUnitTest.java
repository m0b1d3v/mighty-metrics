package dev.m0b1.mighty.metrics.db.coach;

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

class DbCoachMapperUnitTest extends UnitTestBase {

  @InjectMocks
  private DbCoachMapper mapper;

  @Mock
  private ResultSet resultSet;

  @BeforeEach
  public void beforeEach() throws SQLException {
    when(resultSet.getLong(DbCoach.COLUMN_ID)).thenReturn(1L);
    when(resultSet.getString(DbCoach.COLUMN_NAME)).thenReturn("a");
  }

  @Test
  void mapRow() throws SQLException {

    var result = mapper.mapRow(resultSet, 0);

    assertNotNull(result);
    assertEquals(1L, result.getId());
    assertEquals("a", result.getName());
  }

}
