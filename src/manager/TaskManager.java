package manager;

import tasks.*;

import java.util.List;

public interface TaskManager {
    Task createTask(Task task);

    Epic createEpic(Epic epic);

    Subtask createSubtask(Subtask subtask);

    boolean updateTask(Task task);

    boolean updateEpic(Epic epic);

    boolean updateSubtask(Subtask subtask);

    List<Subtask> getEpicSubtasks(Integer epicId);

    Task deleteTaskById(Integer id);

    Epic deleteEpicById(Integer id);

    Subtask deleteSubtaskById(Integer id);

    Task getTaskById(Integer id);

    Epic getEpicById(Integer id);

    Subtask getSubtaskById(Integer id);

    List<Task> getAllTasks();

    List<Epic> getAllEpics();

    List<Subtask> getAllSubtasks();

    void deleteAllTasks();

    void deleteAllSubtasks();

    void deleteAllEpics();

    List<Task> getHistory();

    List<Task> getPrioritizedTasks();

    @Override
    String toString();
}
