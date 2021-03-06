package eu.navispeed.extractor.domain.provider;

import com.github.kiulian.downloader.OnYoutubeDownloadListener;
import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.YoutubeException;
import com.github.kiulian.downloader.model.YoutubeVideo;
import com.github.kiulian.downloader.model.formats.AudioFormat;
import com.github.kiulian.downloader.model.formats.Format;
import com.github.kiulian.downloader.model.formats.VideoFormat;
import com.google.gson.Gson;
import eu.navispeed.extractor.model.Output;
import eu.navispeed.extractor.model.Source;
import eu.navispeed.extractor.model.Task;
import eu.navispeed.extractor.repository.OutputRepository;
import eu.navispeed.extractor.repository.TaskRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

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

  @Override
  public String getName() {
    return "Youtube";
  }

  @Override
  public Optional<Source> addVideoInfo(Source source) {
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

  @Override
  public void download(Task task) {
    String dest = "/tmp/" + task.getId();
    YoutubeVideo video;
    String url = task.getProject().getUrl().getUrl();
    try {
      video = youtubeDownloader.getVideo(url.split("=")[1]);
    } catch (YoutubeException | IOException e) {
      onError(task, "Cannot get video info for" + url);
      return;
    }
    LOGGER.info("Starting download for {}", task.getProject().getUrl());
    VideoFormat videoFormat = video.videoFormats().get(0);
    AudioFormat audioFormat = video.audioFormats().get(0);
    CompletableFuture.allOf(download(task, dest, video, videoFormat),
        download(task, dest, video, audioFormat));
    taskRepository.save(task.toBuilder().state(Task.State.RUNNING).build());
  }

  private CompletableFuture<Void> download(Task task, String dest, YoutubeVideo video,
      Format format) {
    return new CompletableFuture<>().thenRunAsync(() -> {
      try {
        video.downloadAsync(format, new File(dest),
            new OnYoutubeDownloadListener() {
              @Override public void onDownloading(int i) {
                LOGGER.debug("Download progress for {} : {}", task.getProject().getUrl(), i);
                taskRepository.saveAndFlush(task.toBuilder()
                    .state(Task.State.RUNNING)
                    .progress(i)
                    .build());
              }

              @Override public void onFinished(File file) {
                LOGGER.info("Download finish for {}", task.getProject().getUrl());
                File finalOutput =
                    new File(dest + "/output" + (format instanceof VideoFormat ? ".mp4" : ".mp3"));
                Output output = Output.builder()
                    .creationDate(LocalDateTime.now())
                    .project(task.getProject())
                    .isVideo(format instanceof VideoFormat)
                    .isAudio(format instanceof AudioFormat)
                    .path(finalOutput.getAbsolutePath()).build();
                new File(dest).listFiles()[0].renameTo(finalOutput);
                outputRepository.save(output);
                taskRepository.save(
                    task.toBuilder().progress(100).state(Task.State.DONE).output(output).build());
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
        onError(task, "Cannot download video " + video.toString());
      }
    });
  }

  private void onError(Task task, String message) {
    taskRepository
        .save(task.toBuilder().state(Task.State.DONE_WITH_ERROR).message(message).build());
  }


  @Override
  public String getUrlPattern() {
    return ".youtube.";
  }
}
