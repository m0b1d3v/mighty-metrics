package dev.m0b1.mighty.metrics.db.member;

import dev.m0b1.mighty.metrics.UnitTestBase;
import dev.m0b1.mighty.metrics.auth.AuthAttributes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DbMemberRepositoryUnitTest extends UnitTestBase {

  @InjectMocks
  private DbMemberRepository repository;

  @Mock
  private JdbcTemplate jdbcTemplate;

  @Mock
  private OAuth2User oAuth2User;

  private final UUID uuid = UUID.randomUUID();

  @BeforeEach
  public void beforeEach() {
    when(oAuth2User.getAttribute(AuthAttributes.ID)).thenReturn(1);
    when(oAuth2User.getAttribute(AuthAttributes.NAME)).thenReturn("a");
  }

  private static Stream<Arguments> deniedScorecard() {
    return Stream.of(
      Arguments.of(2L, false),
      Arguments.of(1L, false),
      Arguments.of(0L, true),
      Arguments.of(null, true)
    );
  }

  @ParameterizedTest
  @MethodSource
  void deniedScorecard(Long scorecardsFound, boolean expected) {

    when(jdbcTemplate.queryForObject(
      any(String.class),
      eq(Long.class),
      eq(uuid),
      eq(1)
    )).thenReturn(scorecardsFound);

    var result = repository.deniedScorecard(oAuth2User, uuid);

    verify(jdbcTemplate).queryForObject(
      "SELECT COUNT(*) FROM scorecard WHERE uuid = ? AND id_member = ?",
      Long.class,
      uuid,
      1
    );

    assertEquals(expected, result);
  }

  @Test
  void upsert() {

    repository.upsert(oAuth2User);

    var expected = "INSERT INTO member (" +
        "id, " +
        "uuid, " +
        "name" +
      ") VALUES (?, ?, ?) ON CONFLICT (id) DO UPDATE SET " +
        "name = excluded.name"
      ;

    verify(jdbcTemplate).update(eq(expected), eq(1), any(UUID.class), eq(null));
  }

}
