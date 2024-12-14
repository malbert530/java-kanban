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
        if (epics.containsKey(subtask.getEpicId())) {
            int id = nextId();
            subtask.setId(id);
            subtasks.put(subtask.getId(), subtask);
            epics.get(subtask.getEpicId()).updateSubtasksId(id); //добавляем Id подзадачи в список подзадач эпика
            updateEpicStatus(epics.get(subtask.getEpicId())); //обновляем статус эпика
        }
        return subtask;
    }

    public boolean updateTask(Task task) {
        boolean updated = false;
        if (tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task);
            updated = true;
        }
        return updated;
    }

    public boolean updateEpic(Epic epic) {
        boolean updated = false;
        if (epics.containsKey(epic.getId())) {
            epics.get(epic.getId()).setName(epic.getName());
            epics.get(epic.getId()).setDescription(epic.getDescription());
            updated = true;
        }
        return updated;
    }

    public boolean updateSubtask(Subtask subtask) {
        boolean updated = false;
        if (subtasks.containsKey(subtask.getId()) && subtask.getEpicId().equals(subtasks.get(subtask.getId()).getEpicId())) {
            subtasks.put(subtask.getId(), subtask);
            updateEpicStatus(epics.get(subtask.getEpicId())); //обновили статус эпика в связи с новым статусов подзадачи
            updated = true;
        }
        return updated;
    }

    private void updateEpicStatus(Epic epic) {
        Status epicStatus;
        int epicDone = 0;
        int epicNew = 0;
        int epicInProgress = 0;
        if (subtasks.isEmpty() || epic.getSubtasksId().isEmpty()) {
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

    public ArrayList<Subtask> getEpicSubtasks(Integer epicId) {
        ArrayList<Subtask> epicSubtasks = new ArrayList<>();
        if (epics.containsKey(epicId)) {
            for (Integer subtaskId : epics.get(epicId).getSubtasksId()) {
                epicSubtasks.add(subtasks.get(subtaskId));
            }
        }
        return epicSubtasks;
    }

    public Task deleteTaskById(Integer id) {
        return tasks.remove(id);
    }

    public Epic deleteEpicById(Integer id) {
        if (epics.containsKey(id)) {
            for (Integer subtaskId : epics.get(id).getSubtasksId()) {
                subtasks.remove(subtaskId);
            }
        }
        return epics.remove(id);
    }

    public Subtask deleteSubtaskById(Integer id) {
        if (subtasks.containsKey(id)) {
            int epicId = subtasks.get(id).getEpicId(); //вычисляем id эпика, куда входит подзадача
            epics.get(epicId).deleteSubtaskId(id); //удаляем id подзадачи из списка подзадач эпика
            updateEpicStatus(epics.get(epicId));
        }
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
            epic.clearSubtasksId();
            updateEpicStatus(epic);
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
