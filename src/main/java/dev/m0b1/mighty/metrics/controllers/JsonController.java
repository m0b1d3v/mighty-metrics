package dev.m0b1.mighty.metrics.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class JsonController {

  @GetMapping("/json")
  public Map<String, String> json() {
    return Map.of("Hello", "world");
  }

}
