package eu.navispeed.extractor.domain;

import eu.navispeed.extractor.domain.provider.VideoProvider;
import eu.navispeed.extractor.model.DownloadParameter;
import eu.navispeed.extractor.model.ExtractionParameter;
import eu.navispeed.extractor.model.Project;
import eu.navispeed.extractor.model.Source;
import eu.navispeed.extractor.model.Task;
import eu.navispeed.extractor.repository.ProjectRepository;
import eu.navispeed.extractor.repository.SourceRepository;
import eu.navispeed.extractor.repository.TaskRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
@Transactional
public class ProjectService {
  private final ProjectRepository projectRepository;
  private final SourceRepository sourceRepository;
  private final TaskRepository taskRepository;
  private final Set<VideoProvider> videoProviders;

  public ProjectService(ProjectRepository projectRepository, SourceRepository sourceRepository,
      TaskRepository taskRepository,
      Set<VideoProvider> videoProviders) {
    this.projectRepository = projectRepository;
    this.sourceRepository = sourceRepository;
    this.taskRepository = taskRepository;
    this.videoProviders = videoProviders;
  }

  public Iterable<Project> list() {
    return projectRepository.findAll();
  }

  public Optional<Project> get(UUID uuid) {
    return projectRepository.findById(uuid);
  }

  public Optional<Project> create(String url) {
    return generateInfo(Source.builder().url(url).build()).map(source -> {
      sourceRepository.save(source);
      return projectRepository.save(Project.builder().url(source).build());
    });
  }

  public Optional<Task> download(UUID id, int quality) {
    return projectRepository.findById(id).map(p -> taskRepository.save(
        Task.builder().project(p).state(Task.State.TODO).type(Task.Type.DOWNLOAD)
            .downloadParameter(DownloadParameter.builder().videoQuality(quality).build()).build()));
  }

  public Optional<Task> extract(UUID id, UUID input, ExtractionParameter parameter) {
    return projectRepository.findById(id)
        .flatMap(p -> taskRepository.findById(input).map(task -> Map.entry(p, task)))
        .map(p -> {
          ExtractionParameter build = parameter.toBuilder()
              .fromTimeCode(parameter.getFromTimeCode())
              .toTimeCode(parameter.getToTimeCode())
              .extractionTask(p.getValue())
              .build();
          return taskRepository.save(Task.builder().project(p.getKey())
              .state(Task.State.TODO).type(Task.Type.EXTRACTION)
              .extractionParameter(build)
              .build());
        });
  }

  private Optional<Source> generateInfo(Source source) {
    return videoProviders.stream()
        .filter(p -> source.getUrl().contains(p.getUrlPattern()))
        .findFirst().flatMap(p -> p.addVideoInfo(source));
  }

}
