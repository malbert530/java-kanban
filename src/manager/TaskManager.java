package manager;

import tasks.*;

import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private final HashMap<Integer, Subtask> subtasks = new HashMap<>();
    private int counter = 0;

    public Task createTask(Task task) {
        int id = nextId();
        task.setId(id);
        tasks.put(task.getId(), task);
        return task;
    }

    public Epic createEpic(Epic epic) {
        int id = nextId();
        epic.setId(id);
        epics.put(epic.getId(), epic);
        return epic;
    }

    public Subtask createSubtask(Subtask subtask) {
        int id = nextId();
        subtask.setId(id);
        subtasks.put(subtask.getId(), subtask);
        epics.get(subtask.getEpicId()).updateSubtasksId(id); //добавляем Id подзадачи в список подзадач эпика
        return subtask;
    }

    public Task updateTask(Task task) {
        tasks.put(task.getId(), task);
        return task;
    }

    public Epic updateEpic(Epic epic) {
        epics.put(epic.getId(), epic);
        return epic;
    }

    public Subtask updateSubtask(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
        updateEpicStatus(epics.get(subtask.getEpicId())); //обновили статус эпика в связи с новым статусов подзадачи
        return subtask;
    }

    private void updateEpicStatus(Epic epic) {
        Status epicStatus;
        int epicDone = 0;
        int epicNew = 0;
        int epicInProgress = 0;
        if (epic.getSubtasksId() == null) {
            epicNew++;
        } else {
            for (Integer subtaskId : epic.getSubtasksId()) {
                if (subtasks.get(subtaskId).getStatus().equals(Status.IN_PROGRESS)) {
                    epicInProgress++;
                    break;
                } else if (subtasks.get(subtaskId).getStatus().equals(Status.NEW)) {
                    epicNew++;
                } else {
                    epicDone++;
                }
            }
        }
        if (epicInProgress > 0) {
            epicStatus = Status.IN_PROGRESS;
        } else if (epicNew == 0) {
            epicStatus = Status.DONE;
        } else if (epicDone == 0) {
            epicStatus = Status.NEW;
        } else {
            epicStatus = Status.IN_PROGRESS;
        }
        epic.setStatus(epicStatus);
    }

    public ArrayList<Subtask> getEpicSubtasks(Epic epic) {
        ArrayList<Subtask> epicSubtasks = new ArrayList<>();
        for (Integer subtaskId : epic.getSubtasksId()) {
            epicSubtasks.add(subtasks.get(subtaskId));
        }
        return epicSubtasks;
    }

    public Task deleteTaskById(Integer id) {
        return tasks.remove(id);
    }

    public Epic deleteEpicById(Integer id) {
        for (Integer subtaskId : epics.get(id).getSubtasksId()) {
            subtasks.remove(subtaskId);
        }
        return epics.remove(id);
    }

    public Subtask deleteSubtaskById(Integer id) {
        int epicId = subtasks.get(id).getEpicId(); //вычисляем id эпика, куда входит подзадача
        epics.get(epicId).getSubtasksId().remove(id); //удаляем id подзадачи из списка подзадач эпика
        return subtasks.remove(id);
    }

    public Task getTaskById(Integer id) {
        return tasks.get(id);
    }

    public Epic getEpicById(Integer id) {
        return epics.get(id);
    }

    public Subtask getSubtaskById(Integer id) {
        return subtasks.get(id);
    }

    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    public ArrayList<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    public void deleteAllTasks() {
        tasks.clear();
    }

    public void deleteAllSubtasks() {
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.setStatus(Status.NEW);
            epic.getSubtasksId().clear();
        }
    }

    public void deleteAllEpics() {
        epics.clear();
        subtasks.clear();
    }

    private int nextId() {
        return counter++;
    }

    @Override
    public String toString() {
        return "TaskManager{" +
                "tasks=" + tasks +
                ", epics=" + epics +
                ", subtasks=" + subtasks +
                '}';
    }
}
