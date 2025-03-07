package manager;

import tasks.*;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();
    protected int counter = 0;
    private HistoryManager historyManager;
    protected final TreeSet<Task> prioritizedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime));

    public InMemoryTaskManager() {
        this.historyManager = Managers.getDefaultHistory();
    }

    @Override
    public Task createTask(Task task) {
        if (task.getStartTime() == null || isNotCross(task)) {
            int id = nextId();
            task.setId(id);
            tasks.put(task.getId(), task);
            if (task.getStartTime() != null) {
                prioritizedTasks.add(task);
            }
        }
        return task;
    }

    protected boolean isNotCross(Task newTask) {
        Optional<Task> cross = prioritizedTasks.stream()
                .filter(task -> !task.getId().equals(newTask.getId()))
                .filter(task -> task.getStartTime().isBefore(newTask.getEndTime())
                        && task.getEndTime().isAfter(newTask.getStartTime()))
                .findFirst();
        return cross.isEmpty();
    }

    @Override
    public Epic createEpic(Epic epic) {
        int id = nextId();
        epic.setId(id);
        epics.put(epic.getId(), epic);
        return epic;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        if (epics.containsKey(subtask.getEpicId())) {
            if (subtask.getStartTime() == null || isNotCross(subtask)) {
                int id = nextId();
                subtask.setId(id);
                subtasks.put(subtask.getId(), subtask);
                epics.get(subtask.getEpicId()).updateSubtasksId(id); //добавляем Id подзадачи в список подзадач эпика
                updateEpicStatus(epics.get(subtask.getEpicId())); //обновляем статус эпика
                if (subtask.getStartTime() != null) {
                    prioritizedTasks.add(subtask);
                    setEpicTime(epics.get(subtask.getEpicId()));
                }
            }
        }
        return subtask;
    }

    protected void setEpicTime(Epic epic) {
        Optional<Instant> startEpic = epic.getSubtasksId()
                .stream().map(subtasks::get)
                .map(Task::getStartTime)
                .filter(Objects::nonNull)
                .sorted().findFirst();
        if (startEpic.isPresent()) {
            epic.setStartTime(startEpic.get());
        } else {
            epic.setStartTime(null);
        }

        List<Duration> list = epic.getSubtasksId()
                .stream().map(subtasks::get)
                .map(Subtask::getDuration).filter(Objects::nonNull)
                .toList();
        Duration epicDuration = null;
        for (Duration duration : list) {
            if (epicDuration == null) {
                epicDuration = duration;
            } else {
                epicDuration = epicDuration.plus(duration);
            }
        }
        epic.setDuration(epicDuration);

        epic.getSubtasksId()
                .stream().map(subtasks::get)
                .map(Task::getEndTime)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder()).ifPresentOrElse(epic::setEndTime, () -> epic.setEndTime(null));
    }

    @Override
    public boolean updateTask(Task task) {
        boolean updated = false;
        if (tasks.containsKey(task.getId())) {
            if (task.getStartTime() == null || isNotCross(task)) {
                tasks.put(task.getId(), task);
                updated = true;
                if (task.getStartTime() != null) {
                    prioritizedTasks.add(task);
                }
            }
        }
        return updated;
    }

    @Override
    public boolean updateEpic(Epic epic) {
        boolean updated = false;
        if (epics.containsKey(epic.getId())) {
            epics.get(epic.getId()).setName(epic.getName());
            epics.get(epic.getId()).setDescription(epic.getDescription());
            updated = true;
        }
        return updated;
    }

    @Override
    public boolean updateSubtask(Subtask subtask) {
        boolean updated = false;
        if (subtasks.containsKey(subtask.getId())
                && subtask.getEpicId().equals(subtasks.get(subtask.getId()).getEpicId())) {
            if (subtask.getStartTime() == null || isNotCross(subtask)) {
                subtasks.put(subtask.getId(), subtask);
                updateEpicStatus(epics.get(subtask.getEpicId())); //обновили статус эпика в связи с новым статусов подзадачи
                updated = true;
                if (subtask.getStartTime() != null) {
                    prioritizedTasks.add(subtask);
                    setEpicTime(epics.get(subtask.getEpicId()));
                }
            }
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

    @Override
    public List<Subtask> getEpicSubtasks(Integer epicId) {
        List<Subtask> epicSubtasks = new ArrayList<>();
        if (epics.containsKey(epicId)) {
            epicSubtasks = epics.get(epicId).getSubtasksId().stream()
                    .map(subtasks::get).toList();
        }
        return epicSubtasks;
    }

    @Override
    public Task deleteTaskById(Integer id) {
        prioritizedTasks.remove(tasks.get(id));
        historyManager.remove(id);
        return tasks.remove(id);
    }

    @Override
    public Epic deleteEpicById(Integer id) {
        if (epics.containsKey(id)) {
            for (Integer subtaskId : epics.get(id).getSubtasksId()) {
                prioritizedTasks.remove(subtasks.get(id));
                subtasks.remove(subtaskId);
                historyManager.remove(subtaskId);
            }
        }
        historyManager.remove(id);
        return epics.remove(id);
    }

    @Override
    public Subtask deleteSubtaskById(Integer id) {
        if (subtasks.containsKey(id)) {
            int epicId = subtasks.get(id).getEpicId(); //вычисляем id эпика, куда входит подзадача
            epics.get(epicId).deleteSubtaskId(id); //удаляем id подзадачи из списка подзадач эпика
            updateEpicStatus(epics.get(epicId));
            setEpicTime(epics.get(epicId));
        }
        prioritizedTasks.remove(subtasks.get(id));
        historyManager.remove(id);
        return subtasks.remove(id);
    }

    @Override
    public Task getTaskById(Integer id) {
        historyManager.add(tasks.get(id));
        return tasks.get(id);
    }

    @Override
    public Epic getEpicById(Integer id) {
        historyManager.add(epics.get(id));
        return epics.get(id);
    }

    @Override
    public Subtask getSubtaskById(Integer id) {
        historyManager.add(subtasks.get(id));
        return subtasks.get(id);
    }

    @Override
    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public ArrayList<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void deleteAllTasks() {
        for (Integer id : tasks.keySet()) {
            prioritizedTasks.remove(tasks.get(id));
            historyManager.remove(id);
        }
        tasks.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        for (Integer id : subtasks.keySet()) {
            prioritizedTasks.remove(subtasks.get(id));
            historyManager.remove(id);
        }
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.clearSubtasksId();
            updateEpicStatus(epic);
            setEpicTime(epic);
        }
    }

    @Override
    public void deleteAllEpics() {
        for (Integer id : epics.keySet()) {
            historyManager.remove(id);
        }
        for (Integer id : subtasks.keySet()) {
            prioritizedTasks.remove(subtasks.get(id));
            historyManager.remove(id);
        }
        epics.clear();
        subtasks.clear();
    }

    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    private int nextId() {
        return ++counter;
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