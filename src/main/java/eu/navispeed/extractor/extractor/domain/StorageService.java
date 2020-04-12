package eu.navispeed.extractor.extractor.domain;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
public class StorageService {
  private final OutputService outputService;

  public StorageService(OutputService outputService) {
    this.outputService = outputService;
  }

  @Scheduled(cron = "${service.storage.cron}")
  @Transactional
  public void clean() {
    outputService.deleteAllExpiredOutput();
  }
}
