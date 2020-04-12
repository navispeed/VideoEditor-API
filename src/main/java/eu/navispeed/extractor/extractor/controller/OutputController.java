package eu.navispeed.extractor.extractor.controller;

import static org.springframework.web.servlet.function.RouterFunctions.route;
import static org.springframework.web.servlet.function.ServerResponse.ok;

import eu.navispeed.extractor.extractor.domain.ProjectService;
import lombok.Data;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.UUID;

@Component
public class OutputController {
  private final ProjectService service;

  public OutputController(ProjectService projectService) {
    this.service = projectService;
  }

  public RouterFunction<ServerResponse> list() {
    return route().GET("/", req -> ok().body(req.pathVariable("id"))).build();
  }

  public RouterFunction<ServerResponse> allRoutes() {
    return route().path("/project/{id}/output", projectUrl -> list()).build();
  }
}
