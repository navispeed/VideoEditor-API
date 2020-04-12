package eu.navispeed.extractor.extractor.domain;

import static eu.navispeed.extractor.extractor.model.Task.State;

import com.google.common.base.Joiner;
import eu.navispeed.extractor.extractor.domain.provider.VideoProvider;
import eu.navispeed.extractor.extractor.model.Task;
import eu.navispeed.extractor.extractor.repository.TaskRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

@Service
@Slf4j
public class DownloadService {

  public static final Task.Type TARGET_TYPE = Task.Type.DOWNLOAD;
  private final TaskRepository taskRepository;
  private final Set<VideoProvider> videoProviders;


  public DownloadService(TaskRepository taskRepository,
      Set<VideoProvider> videoProviders, ExecutorService executorService) {
    this.taskRepository = taskRepository;
    this.videoProviders = videoProviders;
  }

  @Scheduled(fixedRateString = "${service.download.rate}",
      initialDelayString = "${service.download.initialDelay}")
  @Transactional
  public void checkTodoTask() {
    LOGGER.info("Looking for TODO tasks");
    final List<Task> toDoTask =
        taskRepository.findAllByStateEqualsAndTypeEquals(State.TODO, TARGET_TYPE);
    if (toDoTask.isEmpty()) {
      return;
    }
    LOGGER.info("Found following task to do: {}", Joiner.on(",").join(toDoTask));
    for (Task task : toDoTask) {
      videoProviders.stream()
          .filter(p -> task.getProject().getUrl().getUrl().contains(p.getUrlPattern()))
          .findFirst().ifPresentOrElse(p -> p.download(task), () -> {
        taskRepository.save(task.toBuilder().state(Task.State.DONE_WITH_ERROR)
            .message("No provider was found for this video").build());
        LOGGER.error("No provider was found for task {}", task);
      });
    }
  }
}
