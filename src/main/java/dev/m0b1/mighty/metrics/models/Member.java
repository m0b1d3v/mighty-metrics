package dev.m0b1.mighty.metrics.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.UUID;

@Data
public class Member {

  @JsonIgnore
  private Long id;

  private UUID uuid;

  private String name;

}
