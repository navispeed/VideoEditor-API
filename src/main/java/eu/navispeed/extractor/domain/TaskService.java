package eu.navispeed.extractor.domain;

import eu.navispeed.extractor.model.Task;
import eu.navispeed.extractor.repository.TaskRepository;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
class TaskService {

  private final Task.Type type;
  private final TaskRepository taskRepository;

  TaskService(Task.Type type, TaskRepository taskRepository) {
    this.type = type;
    this.taskRepository = taskRepository;
  }

  Optional<Task> findTODOTask() {
    LOGGER.info("Looking for TODO tasks of type {}", type.name());
    Optional<Task> todoTask =
        taskRepository.findFirstByStateEqualsAndTypeEquals(Task.State.TODO, type);
    todoTask.ifPresent(t -> LOGGER.info("Found following task to do: {}", t));
    return todoTask;
  }

  void onError(Task task, String message) {
    onChange(task, message, Task.State.DONE_WITH_ERROR);
  }

  void onChange(Task task, String message, Task.State state) {
    taskRepository
        .save(task.toBuilder().message(message).state(state).build());
  }
}
