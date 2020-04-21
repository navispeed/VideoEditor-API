package eu.navispeed.extractor.repository;

import eu.navispeed.extractor.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {
  Optional<Task> findFirstByStateEqualsAndTypeEquals(Task.State state, Task.Type type);

  @Query(value = "SELECT new Task(t.id, t.progress,t.state) FROM Task t WHERE t.project.uuid = "
      + ":uuid")
  List<Task> findTaskStatusByProjectUuid(UUID uuid);
}
