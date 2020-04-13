package eu.navispeed.extractor.repository;

import eu.navispeed.extractor.model.Project;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface ProjectRepository extends CrudRepository<Project, UUID> {

}
