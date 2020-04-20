package eu.navispeed.extractor.repository;

import eu.navispeed.extractor.model.Task;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepository extends CrudRepository<Task, UUID> {

  interface PartialTask {
    UUID getId();
    Task.State getState();
    Long getProgress();
  }

  Optional<Task> findFirstByStateEqualsAndTypeEquals(Task.State state, Task.Type type);

  @Query(value = "SELECT t.id, t.progress,t.state FROM Task t WHERE t.project.uuid = :uuid")
  List<PartialTask> findTaskStatusByProjectUuid(UUID uuid);
}
