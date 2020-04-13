package eu.navispeed.extractor.domain;

import eu.navispeed.extractor.model.Output;
import eu.navispeed.extractor.repository.OutputRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class OutputService {

  @Value("${service.storage.expiration}")
  public Integer expirationAfterNHours;

  private final OutputRepository outputRepository;

  public OutputService(OutputRepository outputRepository) {
    this.outputRepository = outputRepository;
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
      if (folder != null) {
        for (File file : folder.listFiles()) {
          file.delete();
        }
        folder.delete();
      }
      outputRepository.save(output.toBuilder().isExpired(true).build());
    });
  }

}
