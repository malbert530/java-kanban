package manager;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Status;
import tasks.Subtask;
import tasks.Task;

import java.io.*;
import java.nio.file.Files;
import java.util.List;

public class FileBackedTaskManagerTest {
    private TaskManager manager;
    private File file;

    @BeforeEach
    public void initManager() {
        try {
            file = File.createTempFile("dataTemp", ".csv");
            manager = new FileBackedTaskManager(file);
        } catch (IOException e) {
            System.out.println("Ошибка при инициализации менеджера " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Test
    void loadFromFile() {
        manager = FileBackedTaskManager.loadFromFile(new File("test/manager/testData.csv"));
        assertEquals(new Task(0, "Task0", "Description0", Status.NEW), manager.getTaskById(0));
        assertEquals(new Subtask(6, "Subtask6", "Description6", Status.NEW, 3), manager.getSubtaskById(6));
    }

    @Test
    void loadFromEmptyFile() {
        manager = FileBackedTaskManager.loadFromFile(new File("test/manager/emptyData.csv"));
        assertTrue(manager.getAllTasks().isEmpty());
        assertTrue(manager.getAllEpics().isEmpty());
        assertTrue(manager.getAllSubtasks().isEmpty());
    }

    @Test
    void deleteAllEpics() {
        Epic epic1 = new Epic("Epic1", "Description1");
        Epic epic2 = new Epic("Epic2", "Description2");
        manager.createEpic(epic1);
        manager.createEpic(epic2);
        try {
            List<String> allLines = Files.readAllLines(file.toPath());
            assertFalse(allLines.isEmpty());

            manager.deleteAllEpics();
            allLines = Files.readAllLines(file.toPath());
            assertTrue(allLines.isEmpty());
        } catch (IOException e) {
            System.out.println("Ошибка при чтении файла " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Test
    void deleteAllSubtasks() {
        Epic epic1 = new Epic("Epic1", "Description1");
        manager.createEpic(epic1);
        Subtask subtask1 = new Subtask("Subtask1", "Description3", Status.NEW, epic1.getId());
        Subtask subtask2 = new Subtask("Subtask2", "Description4", Status.NEW, epic1.getId());
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);
        manager.deleteAllSubtasks();
        try {
            List<String> allLines = Files.readAllLines(file.toPath());
            assertEquals(allLines.getLast(), epic1.toString());
        } catch (IOException e) {
            System.out.println("Ошибка при чтении файла " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Test
    void deleteAllTasks() {
        Task task1 = new Task("Задача 1", "Описание задачи 1", Status.NEW);
        Task task2 = new Task("Задача 2", "Описание задачи 2", Status.NEW);

        manager.createTask(task1);
        manager.createTask(task2);
        try {
            List<String> allLines = Files.readAllLines(file.toPath());
            assertFalse(allLines.isEmpty());

            manager.deleteAllTasks();
            allLines = Files.readAllLines(file.toPath());
            assertTrue(allLines.isEmpty());
        } catch (IOException e) {
            System.out.println("Ошибка при чтении файла " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Test
    void deleteSubtaskById() {
        Epic epic1 = new Epic("Epic1", "Description1");
        manager.createEpic(epic1);
        Subtask subtask1 = new Subtask("Subtask1", "Description3", Status.NEW, epic1.getId());
        manager.createSubtask(subtask1);
        manager.deleteSubtaskById(subtask1.getId());
        try {
            List<String> allLines = Files.readAllLines(file.toPath());
            assertEquals(allLines.getLast(), epic1.toString());
        } catch (IOException e) {
            System.out.println("Ошибка при чтении файла " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Test
    void deleteEpicById() {
        Epic epic1 = new Epic("Epic1", "Description1");
        manager.createEpic(epic1);
        try {
            List<String> allLines = Files.readAllLines(file.toPath());
            assertFalse(allLines.isEmpty());

            manager.deleteEpicById(epic1.getId());
            allLines = Files.readAllLines(file.toPath());
            assertTrue(allLines.isEmpty());
        } catch (IOException e) {
            System.out.println("Ошибка при чтении файла " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Test
    void deleteTaskById() {
        Task task = new Task("Задача 1", "Описание задачи 1", Status.NEW);
        manager.createTask(task);
        try {
            List<String> allLines = Files.readAllLines(file.toPath());
            assertFalse(allLines.isEmpty());

            manager.deleteTaskById(task.getId());
            allLines = Files.readAllLines(file.toPath());
            assertTrue(allLines.isEmpty());
        } catch (IOException e) {
            System.out.println("Ошибка при чтении файла " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Test
    void updateSubtask() {
        Epic epic = new Epic("Эпик 1", "Описание эпика 1");
        manager.createEpic(epic);
        Subtask subtask = new Subtask("Подзадача 1", "Описание подзадачи 1", Status.NEW, epic.getId());
        manager.createSubtask(subtask);
        subtask.setStatus(Status.DONE);
        manager.updateSubtask(subtask);
        try {
            List<String> allLines = Files.readAllLines(file.toPath());
            assertEquals(allLines.getLast(), subtask.toString());
        } catch (IOException e) {
            System.out.println("Ошибка при чтении файла " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Test
    void updateEpic() {
        Epic epic = new Epic("Эпик 1", "Описание эпика 1");
        manager.createEpic(epic);
        epic.setName("Epic 1");
        manager.updateEpic(epic);
        try {
            List<String> allLines = Files.readAllLines(file.toPath());
            assertEquals(allLines.getFirst(), epic.toString());
        } catch (IOException e) {
            System.out.println("Ошибка при чтении файла " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Test
    void updateTask() {
        Task task = new Task("Задача 1", "Описание задачи 1", Status.NEW);
        manager.createTask(task);
        task.setStatus(Status.DONE);
        manager.updateTask(task);
        try {
            List<String> allLines = Files.readAllLines(file.toPath());
            assertEquals(allLines.getFirst(), task.toString());
        } catch (IOException e) {
            System.out.println("Ошибка при чтении файла " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Test
    void createTask() {
        Task task = new Task("Задача 1", "Описание задачи 1", Status.NEW);
        manager.createTask(task);
        try {
            List<String> allLines = Files.readAllLines(file.toPath());
            assertEquals(allLines.getFirst(), task.toString());
        } catch (IOException e) {
            System.out.println("Ошибка при чтении файла " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Test
    void createEpic() {
        Epic epic = new Epic("Эпик 1", "Описание эпика 1");
        manager.createEpic(epic);
        try {
            List<String> allLines = Files.readAllLines(file.toPath());
            assertEquals(allLines.getFirst(), epic.toString());
        } catch (IOException e) {
            System.out.println("Ошибка при чтении файла " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Test
    void createSubtask() {
        Epic epic = new Epic("Эпик 1", "Описание эпика 1");
        manager.createEpic(epic);
        Subtask subtask = new Subtask("Подзадача 1", "Описание подзадачи 1", Status.NEW, epic.getId());
        manager.createSubtask(subtask);
        try {
            List<String> allLines = Files.readAllLines(file.toPath());
            assertEquals(allLines.getFirst(), epic.toString());
            assertEquals(allLines.getLast(), subtask.toString());
        } catch (IOException e) {
            System.out.println("Ошибка при чтении файла " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @AfterEach
    void tearDown() {
        file.deleteOnExit();
    }
}
