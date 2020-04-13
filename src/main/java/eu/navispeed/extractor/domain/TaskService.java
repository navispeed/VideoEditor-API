package eu.navispeed.extractor.domain;

import com.google.common.base.Joiner;
import eu.navispeed.extractor.model.Task;
import eu.navispeed.extractor.repository.TaskRepository;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
class TaskService {

  private final Task.Type type;
  private final TaskRepository taskRepository;

  TaskService(Task.Type type, TaskRepository taskRepository) {
    this.type = type;
    this.taskRepository = taskRepository;
  }

  List<Task> findTODOTask() {
    LOGGER.info("Looking for TODO tasks of type {}", type.name());
    List<Task> todoTask =
        taskRepository.findAllByStateEqualsAndTypeEquals(Task.State.TODO, type);
    LOGGER.info("Found following task to do: {}", Joiner.on(",").join(todoTask));
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
