package dev.m0b1.mighty.metrics.db.member;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.UUID;

@Data
public class DbMember {

  public static final String TABLE = "member";

  public static final String COLUMN_ID = "id";
  public static final String COLUMN_UUID = "uuid";
  public static final String COLUMN_NAME = "name";

  @JsonIgnore
  private Long id;

  private UUID uuid;

  private String name;

}
