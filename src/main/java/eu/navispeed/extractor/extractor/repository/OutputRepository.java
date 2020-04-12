package eu.navispeed.extractor.extractor.repository;

import eu.navispeed.extractor.extractor.model.Output;
import eu.navispeed.extractor.extractor.model.Source;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OutputRepository extends CrudRepository<Output, Integer> {

  List<Output> findAllByCreationDateBeforeAndIsExpiredIsFalse(LocalDateTime creationDate);

}
