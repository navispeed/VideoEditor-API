package eu.navispeed.extractor.controller;

import static java.lang.Math.min;
import static org.springframework.web.servlet.function.RouterFunctions.route;
import static org.springframework.web.servlet.function.ServerResponse.ok;

import eu.navispeed.extractor.domain.OutputService;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
public class OutputController {
  private final OutputService service;


  public OutputController(OutputService projectService) {
    this.service = projectService;
  }

  public RouterFunction<ServerResponse> list() {
    return route().GET("/", req -> ok().body(service.list(UUID.fromString(req.pathVariable("id")))))
        .build();
  }

  public RouterFunction<ServerResponse> get() {
    return route().GET("/", req -> ok().body(service.get(Integer.valueOf(req.pathVariable("id")))))
        .build();
  }

  public RouterFunction<ServerResponse> stream() {
    return route().GET("/stream", req -> {
      Optional<UrlResource> id = service.getUrlFor(Integer.valueOf(req.pathVariable("id")));
      return id.map(url -> ServerResponse.status(HttpStatus.PARTIAL_CONTENT).contentType(
              MediaTypeFactory.getMediaType(url).orElse(MediaType.APPLICATION_OCTET_STREAM))
              .body(resourceRegion(url, req.headers().asHttpHeaders())))
          .orElse(ServerResponse.notFound().build());
    }).onError(IOException.class,
        (throwable, serverRequest) -> ServerResponse.status(501).build()).build();
  }

  public RouterFunction<ServerResponse> allRoutes() {
    return route().path("/project/{id}/output", projectUrl -> projectUrl.add(list())).path(
        "/output/{id}",
        outputUrl -> outputUrl.add(get()).add(stream())).build();
  }

  @SneakyThrows
  private ResourceRegion resourceRegion(UrlResource video, HttpHeaders headers) {
    val contentLength = video.contentLength();
    val range = headers.getRange().get(0);
    if (range != null) {
      val start = range.getRangeStart(contentLength);
      val end = range.getRangeEnd(contentLength);
      val rangeLength = min(1024 * 1024, end - start + 1);
      return new ResourceRegion(video, 0, rangeLength);
    } else {
      val rangeLength = min(1024 * 1024, contentLength);
      return new ResourceRegion(video, 0, rangeLength);
    }
  }
}
