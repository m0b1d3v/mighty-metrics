package dev.m0b1.mighty.metrics.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HtmlController {

  @GetMapping("/")
  public String html(Model model) {
    model.addAttribute("greeting", "Hello, world.");
    return "index";
  }

}
