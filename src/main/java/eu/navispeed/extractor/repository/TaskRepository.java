package eu.navispeed.extractor.repository;

import eu.navispeed.extractor.model.Project;
import eu.navispeed.extractor.model.Task;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepository extends CrudRepository<Task, UUID> {
  Optional<Task> findFirstByStateEqualsAndTypeEquals(Task.State state, Task.Type type);
  List<Task> findAllByProjectEqualsAndStateIsNotAndTypeEquals(Project project, Task.State state,
      Task.Type type);

}
