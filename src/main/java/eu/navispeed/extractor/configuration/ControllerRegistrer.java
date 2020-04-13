package eu.navispeed.extractor.configuration;

import static org.springframework.web.servlet.function.RouterFunctions.route;
import static org.springframework.web.servlet.function.ServerResponse.notFound;
import static org.springframework.web.servlet.function.ServerResponse.status;

import eu.navispeed.extractor.controller.OutputController;
import eu.navispeed.extractor.controller.ProjectController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
@Slf4j
public class ControllerRegistrer {

  @Bean RouterFunction<ServerResponse> allApplicationRoutes(ProjectController projectController,
      OutputController outputController) {
    return route()
        .add(projectController.allRoutes()).add(outputController.allRoutes())
        .before(req -> {
          LOGGER.info("Found a route which matches " + req.uri().getPath());
          return req;
        }).after((req, res) -> {
          if (res.statusCode() == HttpStatus.OK) {
            LOGGER.info("Finished processing request " + req.uri().getPath());
          } else {
            LOGGER.info("There was an error while processing request" + req.uri());
          }
          return res;
        }).onError(IllegalArgumentException.class, (e, res) -> {
          LOGGER.error("Bad argument was passed", e);
          return status(HttpStatus.BAD_REQUEST).build();
        }).onError(Throwable.class, (e, res) -> {
          LOGGER.error("Fatal exception has occurred", e);
          return status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }).build().and(route(RequestPredicates.all(), req -> notFound().build()));
  }

}
