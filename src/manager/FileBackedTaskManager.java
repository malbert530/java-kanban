package manager;

import exception.ManagerLoadFileException;
import exception.ManagerSaveException;
import tasks.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private File data;

    public FileBackedTaskManager(File data) {
        super();
        this.data = data;
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        try {
            FileBackedTaskManager manager = new FileBackedTaskManager(file);
            List<String> allLines = Files.readAllLines(file.toPath());
            for (String line : allLines) {
                String[] note = line.split(",");
                if (TaskTypes.valueOf(note[1]).equals(TaskTypes.TASK)) {
                    Task task = createTaskFromString(note);
                    manager.tasks.put(task.getId(), task);
                } else if (TaskTypes.valueOf(note[1]).equals(TaskTypes.EPIC)) {
                    Epic epic = createEpicFromString(note);
                    manager.epics.put(epic.getId(), epic);
                } else if (TaskTypes.valueOf(note[1]).equals(TaskTypes.SUBTASK)) {
                    Subtask subtask = createSubtaskFromString(note);
                    manager.subtasks.put(subtask.getId(), subtask);
                    manager.epics.get(subtask.getEpicId()).updateSubtasksId(subtask.getId());
                }
            }
            manager.counter = findLastId(manager);
            return manager;
        } catch (IOException e) {
            String errorMessage = "Ошибка чтения файла " + e.getMessage();
            System.out.println(errorMessage);
            throw new ManagerLoadFileException(errorMessage);
        }
    }

    private static int findLastId(FileBackedTaskManager mn) {
        Set<Integer> list = new HashSet<>();
        list.addAll(mn.tasks.keySet());
        list.addAll(mn.epics.keySet());
        list.addAll(mn.subtasks.keySet());
        return list.isEmpty() ? 0 : Collections.max(list);
    }

    private static Subtask createSubtaskFromString(String[] str) {
        int id = Integer.parseInt(str[0]);
        int epicId = Integer.parseInt(str[5]);
        String name = str[2];
        String description = str[4];
        Status status = Status.valueOf(str[3]);
        return new Subtask(id, name, description, status, epicId);
    }

    private static Epic createEpicFromString(String[] str) {
        int id = Integer.parseInt(str[0]);
        String name = str[2];
        String description = str[4];
        Status status = Status.valueOf(str[3]);
        return new Epic(id, name, description, status);
    }

    private static Task createTaskFromString(String[] str) {
        int id = Integer.parseInt(str[0]);
        String name = str[2];
        String description = str[4];
        Status status = Status.valueOf(str[3]);
        return new Task(id, name, description, status);
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public Subtask deleteSubtaskById(Integer id) {
        Subtask deletedSubtask = super.deleteSubtaskById(id);
        save();
        return deletedSubtask;
    }

    @Override
    public Epic deleteEpicById(Integer id) {
        Epic deletedEpic = super.deleteEpicById(id);
        save();
        return deletedEpic;
    }

    @Override
    public Task deleteTaskById(Integer id) {
        Task deletedTask = super.deleteTaskById(id);
        save();
        return deletedTask;
    }

    @Override
    public boolean updateSubtask(Subtask subtask) {
        boolean updated = super.updateSubtask(subtask);
        save();
        return updated;
    }

    @Override
    public boolean updateEpic(Epic epic) {
        boolean updated = super.updateEpic(epic);
        save();
        return updated;
    }

    @Override
    public boolean updateTask(Task task) {
        boolean updated = super.updateTask(task);
        save();
        return updated;
    }

    @Override
    public Task createTask(Task task) {
        Task createdTask = super.createTask(task);
        save();
        return createdTask;
    }

    @Override
    public Epic createEpic(Epic epic) {
        Epic createdEpic = super.createEpic(epic);
        save();
        return createdEpic;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        Subtask createdSubtask = super.createSubtask(subtask);
        save();
        return createdSubtask;
    }

    private void save() {
        List<Task> allTasks = getAllTasks();
        List<Epic> allEpics = getAllEpics();
        List<Subtask> allSubtasks = getAllSubtasks();
        List<String> allLines = new ArrayList<>();
        for (Task task : allTasks) {
            allLines.add(task.toString());
        }
        for (Epic epic : allEpics) {
            allLines.add(epic.toString());
        }
        for (Subtask subtask : allSubtasks) {
            allLines.add(subtask.toString());
        }
        writeStringInFile(allLines);
    }

    private void writeStringInFile(List<String> allLines) {
        try (FileWriter fw = new FileWriter(data)) {
            for (String line : allLines) {
                fw.write(line + "\n");
            }
        } catch (IOException e) {
            String errorMessage = "Ошибка при записи в файл " + e.getMessage();
            System.out.println(errorMessage);
            throw new ManagerSaveException(errorMessage);
        }
    }
}
