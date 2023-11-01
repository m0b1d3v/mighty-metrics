package dev.m0b1.mighty.metrics.db.member;

import dev.m0b1.mighty.metrics.auth.AuthAttributes;
import dev.m0b1.mighty.metrics.db.DbUtil;
import dev.m0b1.mighty.metrics.db.scorecard.DbScoreCard;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class DbMemberRepository {

  private final JdbcTemplate jdbcTemplate;

  public boolean deniedScorecard(OAuth2User oAuth2User, UUID uuid) {

    var userId = oAuth2User.getAttribute(AuthAttributes.ID);
    var sql = String.format("SELECT COUNT(*) FROM %s WHERE uuid = ? AND id_member = ?", DbScoreCard.TABLE);
    var matchingColumns = jdbcTemplate.queryForObject(sql, Long.class, uuid, userId);

    return matchingColumns == null || matchingColumns < 1;
  }

  public void upsert(OAuth2User oAuth2User) {

    var inputMap = new LinkedHashMap<String, Object>();
    inputMap.put(DbMember.COLUMN_ID, oAuth2User.getAttribute(AuthAttributes.ID));
    inputMap.put(DbMember.COLUMN_UUID, UUID.randomUUID());
    inputMap.put(DbMember.COLUMN_NAME, oAuth2User.getAttribute(AuthAttributes.GLOBAL_NAME));

    DbUtil.upsert(
      jdbcTemplate,
      inputMap,
      DbMember.COLUMN_NAME::equals, // Only update the name if there is a conflict
      DbMember.TABLE,
      DbMember.COLUMN_ID
    );
  }

}
