package eu.navispeed.extractor.repository;

import eu.navispeed.extractor.model.Output;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface OutputRepository extends CrudRepository<Output, Integer> {

  List<Output> findAllByCreationDateBeforeAndIsExpiredIsFalse(LocalDateTime creationDate);

  List<Output> findAllByProject_UuidEquals(UUID projectId);

}
