package eu.navispeed.extractor.controller;

import static org.springframework.web.servlet.function.RequestPredicates.contentType;
import static org.springframework.web.servlet.function.RouterFunctions.route;
import static org.springframework.web.servlet.function.ServerResponse.notFound;
import static org.springframework.web.servlet.function.ServerResponse.ok;

import eu.navispeed.extractor.domain.ProjectService;
import eu.navispeed.extractor.model.ExtractionParameter;
import lombok.Data;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.UUID;

@Component
public class ProjectController {
  public static final ErrorMessage DOWNLOAD_TASK_ALREADY_EXIST = new ErrorMessage("There is "
      + "already a download task for this project");
  public static final ErrorMessage EXTRACTION_TASK_ALREADY_EXIST = new ErrorMessage("There is "
      + "already an extraction task for this project");
  private final ProjectService service;

  public ProjectController(ProjectService projectService) {
    this.service = projectService;
  }

  public RouterFunction<ServerResponse> list() {
    return route().GET("/", req -> ok().body(service.list())).build();
  }

  public RouterFunction<ServerResponse> create() {
    return route().POST("/", contentType(MediaType.APPLICATION_JSON),
        req -> ok().body(service.create(req.body(ProjectForm.class).getUrl()))).build();
  }

  public RouterFunction<ServerResponse> get() {
    return route().GET("/{id}",
        req -> service.get(UUID.fromString(req.pathVariable("id")))
            .map(project -> ok().body(project))
            .orElse(notFound().build())
    ).build();
  }

  public RouterFunction<ServerResponse> download() {
    return route().POST("/{id}/download", contentType(MediaType.APPLICATION_JSON),
        req -> service
            .download(UUID.fromString(req.pathVariable("id")),
                req.body(DownloadForm.class).getVideoQualityChoice())
            .map(task -> ok().body(task)).orElse(notFound().build()))
        .onError(DataIntegrityViolationException.class,
            (t, req) -> ServerResponse.badRequest().body(DOWNLOAD_TASK_ALREADY_EXIST)).build();
  }

  public RouterFunction<ServerResponse> extract() {
    return route().POST("/{id}/extraction", contentType(MediaType.APPLICATION_JSON),
        req -> {
          ExtractionForm form = req.body(ExtractionForm.class);
          return service.extract(UUID.fromString(req.pathVariable("id")), form.inputTask,
              ExtractionParameter.builder().format(form.format)
                  .fromTimeCode(form.fromTimeCode).toTimeCode(form.toTimeCode)
                  .build())
              .map(task -> ok().body(task)).orElse(notFound().build());
        })
        .onError(DataIntegrityViolationException.class,
            (t, req) -> ServerResponse.badRequest().body(EXTRACTION_TASK_ALREADY_EXIST)).build();
  }

  public RouterFunction<ServerResponse> allRoutes() {
    return route().path("/project", projectUrl -> projectUrl
        .add(create()).add(list()).add(download()).add(get()).add(extract())).build();
  }

  @Data
  private static class ProjectForm {
    String url;
  }


  @Data
  private static class DownloadForm {
    int videoQualityChoice = 0;
  }


  @Data
  private static class ExtractionForm {
    long fromTimeCode = 0;
    long toTimeCode = 0;
    UUID inputTask;
    ExtractionParameter.Format format;
  }

}
