package dev.m0b1.mighty.metrics.db.score;

import lombok.Data;

@Data
public class DbScore {

  public static final String TABLE = "score";

  public static final String COLUMN_ID = "id";
  public static final String COLUMN_VALUE = "value";

  private Integer id;

  private String value;

}
