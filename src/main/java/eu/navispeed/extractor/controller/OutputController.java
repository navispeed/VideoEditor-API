package eu.navispeed.extractor.controller;

import static java.lang.Math.min;
import static org.springframework.web.servlet.function.RouterFunctions.route;
import static org.springframework.web.servlet.function.ServerResponse.ok;

import eu.navispeed.extractor.domain.OutputService;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
public class OutputController {
  private final OutputService service;
  private final long bufferSize;


  public OutputController(OutputService projectService,
      @Value("${service.output.stream.buffer}") String bufferSize) {
    this.service = projectService;
    this.bufferSize = Long.parseLong(bufferSize) * 1024;
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
              .body(readByteRange(url, req.headers().asHttpHeaders())))
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
  public byte[] readByteRange(UrlResource video, HttpHeaders headers) {
    Pair<Long, Long> range = resourceRegion(video, headers);
    FileInputStream inputStream = new FileInputStream(video.getFile());
    ByteArrayOutputStream bufferedOutputStream = new ByteArrayOutputStream();
    byte[] data = new byte[(int) (range.getRight() - range.getLeft())];
    int nRead;
    while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
      bufferedOutputStream.write(data, 0, nRead);
    }
    bufferedOutputStream.flush();
    byte[] result = new byte[(int) (range.getRight() - range.getLeft())];
    System.arraycopy(bufferedOutputStream.toByteArray(), range.getLeft().intValue(), result, 0,
        (int) (range.getRight() - range.getLeft()));
    return result;
  }

  @SneakyThrows
  private Pair<Long, Long> resourceRegion(UrlResource video, HttpHeaders headers) {
    val contentLength = video.contentLength();
    return headers.getRange().stream().findFirst().map(range -> {
      val start = range.getRangeStart(contentLength);
      val end = range.getRangeEnd(contentLength);
      return Pair.of(start, min(end, contentLength));
    }).orElseGet(() -> Pair.of(0L, bufferSize));
  }
}
