package org.onedatashare.server.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.reactive.result.view.RedirectView;

@org.springframework.stereotype.Controller
public class Controller {
  @GetMapping(value = {"", "/"})
  public RedirectView forward() {
    return new RedirectView("/index.html");
  }
}
