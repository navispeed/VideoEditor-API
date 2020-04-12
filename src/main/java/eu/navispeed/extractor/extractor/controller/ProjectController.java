package eu.navispeed.extractor.extractor.controller;

import static org.springframework.web.servlet.function.RouterFunctions.route;
import static org.springframework.web.servlet.function.ServerResponse.notFound;
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
public class ProjectController {
  private final ProjectService service;

  public ProjectController(
      ProjectService projectService) {
    this.service = projectService;
  }

  public RouterFunction<ServerResponse> list() {
    return route().GET("/", req -> ok().body(service.list())).build();
  }

  public RouterFunction<ServerResponse> create() {
    return route().POST("/", RequestPredicates.contentType(MediaType.APPLICATION_JSON),
        req -> ok().body(service.create(req.body(ProjectForm.class).getUrl()))).build();
  }

  public RouterFunction<ServerResponse> download() {
    return route().POST("/{id}/download", RequestPredicates.contentType(MediaType.APPLICATION_JSON),
        req -> service
            .download(UUID.fromString(req.pathVariable("id")),
                req.body(DownloadForm.class).getVideoQualityChoice())
            .map(task -> ok().body(task)).orElse(notFound().build())).build();
  }

  public RouterFunction<ServerResponse> allRoutes() {
    return route().path("/project",
        projectUrl -> projectUrl.add(create()).add(list()).add(download())).build();
  }

  @Data
  private static class ProjectForm {
    String url;
  }


  @Data
  private static class DownloadForm {
    int videoQualityChoice = 0;
  }

}
