package eu.navispeed.extractor.domain;

import eu.navispeed.extractor.model.ExtractionParameter;
import eu.navispeed.extractor.model.ExtractionParameter.Format;
import eu.navispeed.extractor.model.Output;
import eu.navispeed.extractor.model.Task;
import eu.navispeed.extractor.repository.TaskRepository;
import lombok.extern.slf4j.Slf4j;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.builder.FFmpegOutputBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class ExtractionService extends TaskService {
  private final TaskRepository taskRepository;
  private final FFmpegExecutor fFmpegExecutor;
  private final FFmpeg fFmpeg;
  private final FFprobe fFprobe;


  public ExtractionService(TaskRepository taskRepository, FFmpegExecutor fFmpegExecutor,
      FFmpeg fFmpeg, FFprobe fFprobe) {
    super(Task.Type.EXTRACTION, taskRepository);
    this.taskRepository = taskRepository;
    this.fFmpegExecutor = fFmpegExecutor;
    this.fFmpeg = fFmpeg;
    this.fFprobe = fFprobe;
  }

  @Scheduled(fixedRateString = "${service.extraction.rate}",
      initialDelayString = "${service.extraction.initialDelay}")
  public void checkTodoTask() {
    List<Task> todoTask = this.findTODOTask();
    todoTask.forEach(this::processTask);
  }

  private void processTask(Task task) {
    ExtractionParameter extractionParameter = task.getExtractionParameter();
    LOGGER.info("ExtractionParameter: {}", extractionParameter);

    switch (extractionParameter.getExtractionTask().getState()) {
      case DONE:
        break;
      case DONE_WITH_ERROR:
        LOGGER.info("Cancelling task");
        onError(task, "Linked task was not successfully downloaded, cannot make extraction");
        return;
      case TODO:
      case RUNNING:
        LOGGER.debug("{} is not complete, ignoring it for the moment",
            extractionParameter.getExtractionTask());
        return;
    }
    Output output = extractionParameter.getExtractionTask().getOutput();

    if (output.getIsExpired()) {
      onError(task, "The input has expired, please create another download task");
      return;
    }
    String path =
        "/tmp/" + task.getProject().getUuid() + "-" + task.getId() + "-converted." + (
            extractionParameter.getFormat() == Format.VIDEO ?
                "VIDEO" :
                "AUDIO");
    String inputFilePath = new File(output.getPath()).listFiles()[0].getAbsolutePath();
    FFmpegOutputBuilder fFmpegOutputBuilder = fFmpeg.builder()
        .setInput(inputFilePath)
        .overrideOutputFiles(true)
        .addOutput(path)
        .setFormat(extractionParameter.getFormat() == Format.VIDEO ? "mp4" : "mp3")
        .disableSubtitle()
        .setStartOffset(extractionParameter.getFromTimeCode(), TimeUnit.SECONDS)
        .setDuration(extractionParameter.getToTimeCode() - extractionParameter.getFromTimeCode(),
            TimeUnit.SECONDS);
    FFmpegBuilder builder;
    switch (extractionParameter.getFormat()) {
      case VIDEO:
        builder = fFmpegOutputBuilder
            .setVideoCodec("libx264")     // Video using x264
            .setAudioCodec("aac")
            .setStrict(FFmpegBuilder.Strict.EXPERIMENTAL) // Allow FFmpeg to use experimental specs
            .done();
        break;
      case AUDIO:
        fFmpegOutputBuilder.video_enabled = false;
        builder = fFmpegOutputBuilder
            .setAudioCodec("aac")
            .setStrict(FFmpegBuilder.Strict.EXPERIMENTAL) // Allow FFmpeg to use experimental specs
            .done();
        break;
      default:
        onError(task, "Unknown output");
        return;
    }


    final double duration_ns;
    try {
      duration_ns =
          fFprobe.probe(inputFilePath).getFormat().duration * TimeUnit.SECONDS.toNanos(1);
    } catch (IOException e) {
      onError(task, "Cannot apply ffprobe to input " + output + " -> " + e.getMessage());
      return;
    }

    try {
      fFmpegExecutor.createJob(builder/*, progress -> {
      if (System.currentTimeMillis() / 1000 % 10 != 0) { //Update every 10 second
        return;
      }
      taskRepository.save(
          task.toBuilder().progress((int) Math.round(progress.out_time_ns / duration_ns)).build());
    }*/).run();

    } catch (RuntimeException e) {
      onError(task, e.getMessage());
    }
    Output.builder().project(task.getProject()).creationDate(LocalDateTime.now()).path(path);
  }

}
