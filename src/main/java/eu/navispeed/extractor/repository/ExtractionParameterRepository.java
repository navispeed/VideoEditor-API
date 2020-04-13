package eu.navispeed.extractor.repository;

import eu.navispeed.extractor.model.ExtractionParameter;
import eu.navispeed.extractor.model.Source;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface ExtractionParameterRepository extends CrudRepository<ExtractionParameter, UUID> {
}
