package eu.navispeed.extractor.domain;

import eu.navispeed.extractor.model.Output;
import eu.navispeed.extractor.repository.OutputRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.File;
import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class OutputService {

  @Value("${service.storage.expiration}")
  public Integer expirationAfterNHours;

  private final OutputRepository outputRepository;

  public OutputService(OutputRepository outputRepository) {
    this.outputRepository = outputRepository;
  }

  public Optional<Output> get(Integer id) {
    return outputRepository.findById(id);
  }

  @Transactional
  public Iterable<Output> list() {
    return outputRepository.findAll();
  }

  @Transactional
  public List<Output> list(UUID projectId) {
    return outputRepository.findAllByProject_UuidEquals(projectId);
  }

  public Optional<UrlResource> getUrlFor(Integer id) {
    return outputRepository.findById(id).flatMap(o -> {
      try {
        return Optional.of(new UrlResource("file://" + o.getPath()));
      } catch (MalformedURLException e) {
        return Optional.empty();
      }
    });
  }

  void deleteAllExpiredOutput() {
    List<Output> allExpired = outputRepository.findAllByCreationDateBeforeAndIsExpiredIsFalse(
        LocalDateTime.now().minusHours(expirationAfterNHours));
    if (allExpired.isEmpty()) {
      return;
    }
    LOGGER.info("To remove: {}", Arrays.toString(allExpired.toArray(new Output[0])));
    allExpired.forEach(output -> {
      File folder = new File(output.getPath());
      File[] files = folder.listFiles();
      if (files != null) {
        for (File file : files) {
          file.delete();
        }
        folder.delete();
      }
      outputRepository.save(output.toBuilder().isExpired(true).build());
    });
  }

}
