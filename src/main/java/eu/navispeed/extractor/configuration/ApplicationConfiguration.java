package eu.navispeed.extractor.configuration;

import com.github.kiulian.downloader.YoutubeDownloader;
import lombok.SneakyThrows;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ApplicationConfiguration {

  @Bean
  public ExecutorService executorService() {
    return Executors.newSingleThreadExecutor();
  }

  @Bean
  public YoutubeDownloader youtubeDownloader() {
    YoutubeDownloader downloader = new YoutubeDownloader();
    downloader.setParserRequestProperty("User-Agent",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36");
    return downloader;
  }

  @Bean
  @SneakyThrows
  public FFmpeg fFmpeg(@Value("${service.extraction.ffmpeg.location}") String ffmpegPath) {
    return new FFmpeg(ffmpegPath);
  }

  @Bean
  @SneakyThrows
  public FFprobe ffprobe(@Value("${service.extraction.ffprobe.location}") String ffprobePath) {
    return new FFprobe(ffprobePath);
  }

  @Bean
  @SneakyThrows
  public FFmpegExecutor fFmpegExecutor(FFmpeg ffmeg, FFprobe ffprobe) {
    return new FFmpegExecutor(ffmeg, ffprobe);
  }

}
