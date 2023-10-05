package dev.m0b1.mighty.metrics.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EnumScore {

  S_PLUS("S+"),
  S("S"),
  A("A"),
  B("B"),
  C("C"),
  D("D"),
  ;

  private final String value;

}
