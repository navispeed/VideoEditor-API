package eu.navispeed.extractor.repository;

import eu.navispeed.extractor.model.Output;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OutputRepository extends CrudRepository<Output, Integer> {

  List<Output> findAllByCreationDateBeforeAndIsExpiredIsFalse(LocalDateTime creationDate);

}
