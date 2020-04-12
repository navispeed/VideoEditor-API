package eu.navispeed.extractor.extractor.configuration;

import com.github.kiulian.downloader.YoutubeDownloader;
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
    downloader.setParserRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36");
    return downloader;
  }

}
