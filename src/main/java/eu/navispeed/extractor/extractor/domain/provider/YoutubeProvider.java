package eu.navispeed.extractor.extractor.domain.provider;

import com.github.kiulian.downloader.OnYoutubeDownloadListener;
import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.YoutubeException;
import com.github.kiulian.downloader.model.YoutubeVideo;
import com.github.kiulian.downloader.model.formats.VideoFormat;
import com.google.gson.Gson;
import eu.navispeed.extractor.extractor.model.Output;
import eu.navispeed.extractor.extractor.model.Source;
import eu.navispeed.extractor.extractor.model.Task;
import eu.navispeed.extractor.extractor.repository.OutputRepository;
import eu.navispeed.extractor.extractor.repository.TaskRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class YoutubeProvider implements VideoProvider {
  private static final Gson GSON = new Gson();
  private final YoutubeDownloader youtubeDownloader;
  private final TaskRepository taskRepository;
  private final OutputRepository outputRepository;


  public YoutubeProvider(YoutubeDownloader youtubeDownloader, TaskRepository taskRepository,
      OutputRepository outputRepository) {
    this.youtubeDownloader = youtubeDownloader;
    this.taskRepository = taskRepository;
    this.outputRepository = outputRepository;
  }

  @Override public String getName() {
    return "Youtube";
  }

  @Override public Optional<Source> addVideoInfo(Source source) {
    YoutubeVideo video;
    try {
      video = youtubeDownloader.getVideo(source.getUrl().split("=")[1]);
    } catch (YoutubeException | IOException e) {
      LOGGER.error("Cannot get video info for source {}", source, e);
      return Optional.empty();
    }
    return Optional.of(source.toBuilder().videoFormats(GSON.toJson(video.videoFormats()))
        .title(video.details().title()).build());
  }

  @Override public void download(Task task) {
    final boolean anotherAlreadyExist =
        taskRepository.findAllByProjectEqualsAndStateIsNotAndTypeEquals(task.getProject(),
            Task.State.TODO, Task.Type.DOWNLOAD).isEmpty();
    if (anotherAlreadyExist) {
      onError(task, "Another task with the same url is already scheduled");
      return;
    }
    String dest = "/tmp/" + task.getId() + ".mp4";
    YoutubeVideo video;
    String url = task.getProject().getUrl().getUrl();
    try {
      video = youtubeDownloader.getVideo(url.split("=")[1]);
    } catch (YoutubeException | IOException e) {
      onError(task, "Cannot get video info for" + url);
      return;
    }
    LOGGER.info("Starting download for {}", task.getProject().getUrl());
    List<VideoFormat> videoFormats = video.videoFormats();
    try {
      video.downloadAsync(videoFormats.get(0), new File(dest),
          new OnYoutubeDownloadListener() {
            @Override public void onDownloading(int i) {
              LOGGER.debug("Download progress for {} : {}", task.getProject().getUrl(), i);
              taskRepository.save(task.toBuilder()
                  .state(Task.State.DONE_WITH_ERROR)
                  .progress(i)
                  .build());

            }

            @Override public void onFinished(File file) {
              LOGGER.info("Download finish for {}", task.getProject().getUrl());
              Output output = Output.builder().creationDate(LocalDateTime.now()).path(dest).build();
              outputRepository.save(output);
              taskRepository.save(task.toBuilder().state(Task.State.DONE).output(output).build());
            }

            @Override public void onError(Throwable throwable) {
              LOGGER.info("Download error for {}", task.getProject().getUrl());
              taskRepository.save(task.toBuilder()
                  .state(Task.State.DONE_WITH_ERROR)
                  .message(throwable.getMessage())
                  .build());
            }
          });
    } catch (YoutubeException | IOException e) {
      onError(task, "Cannot download video " + url);
    }
    taskRepository.save(task.toBuilder().state(Task.State.RUNNING).build());
  }

  private void onError(Task task, String message) {
    taskRepository
        .save(task.toBuilder().state(Task.State.DONE_WITH_ERROR).message(message).build());
  }


  @Override public String getUrlPattern() {
    return ".youtube.";
  }
}
