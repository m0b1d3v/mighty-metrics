package dev.m0b1.mighty.metrics.db.coach;

import lombok.Data;

@Data
public class DbCoach {

  public static final String TABLE = "coach";

  public static final String COLUMN_ID = "id";
  public static final String COLUMN_NAME = "name";

  private Integer id;

  private String name;

}
