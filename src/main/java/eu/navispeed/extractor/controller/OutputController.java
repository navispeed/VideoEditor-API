package eu.navispeed.extractor.controller;

import static org.springframework.web.servlet.function.RouterFunctions.route;
import static org.springframework.web.servlet.function.ServerResponse.ok;

import eu.navispeed.extractor.domain.ProjectService;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

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
