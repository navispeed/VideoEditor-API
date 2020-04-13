package eu.navispeed.extractor.domain;

import eu.navispeed.extractor.domain.provider.VideoProvider;
import eu.navispeed.extractor.model.Task;
import eu.navispeed.extractor.repository.TaskRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Set;

@Service
@Slf4j
public class DownloadService extends TaskService {

  public static final Task.Type TARGET_TYPE = Task.Type.DOWNLOAD;
  private final TaskRepository taskRepository;
  private final Set<VideoProvider> videoProviders;


  public DownloadService(TaskRepository taskRepository,
      Set<VideoProvider> videoProviders) {
    super(Task.Type.DOWNLOAD, taskRepository);
    this.taskRepository = taskRepository;
    this.videoProviders = videoProviders;
  }

  @Scheduled(fixedRateString = "${service.download.rate}",
      initialDelayString = "${service.download.initialDelay}")
  @Transactional
  public void checkTodoTask() {
    findTODOTask().ifPresent(task -> {
      videoProviders.stream()
          .filter(p -> task.getProject().getUrl().getUrl().contains(p.getUrlPattern()))
          .findFirst().ifPresentOrElse(p -> p.download(task), () -> {
        taskRepository.save(task.toBuilder().state(Task.State.DONE_WITH_ERROR)
            .message("No provider was found for this video").build());
        LOGGER.error("No provider was found for task {}", task);
      });
    });
  }
}
