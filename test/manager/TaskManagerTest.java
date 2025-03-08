package manager;

import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Status;
import tasks.Subtask;
import tasks.Task;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

abstract class TaskManagerTest<M extends TaskManager> {
    protected M manager;

    abstract TaskManager getTaskManager();

    @Test
    void createTaskAndGetTaskById() {
        Task task = new Task("Задача 1", "Описание задачи 1", Status.NEW);

        manager.createTask(task);
        Task savedTask = manager.getTaskById(task.getId());

        assertNotNull(savedTask);
        assertEquals(savedTask, task);
    }

    @Test
    void createEpicAndGetEpicById() {
        Epic epic = new Epic("Эпик 1", "Описание эпика 1");

        manager.createEpic(epic);

        Epic savedEpic = manager.getEpicById(epic.getId());
        assertNotNull(savedEpic);
        assertEquals(savedEpic, epic);
    }

    @Test
    void createSubtaskAndGetSubtaskById() {
        Epic epic = new Epic("Эпик 1", "Описание эпика 1");
        manager.createEpic(epic);
        Subtask subtask = new Subtask("Подзадача 1", "Описание подзадачи 1", Status.NEW, epic.getId());
        manager.createSubtask(subtask);

        Subtask savedSubtask = manager.getSubtaskById(subtask.getId());

        assertNotNull(savedSubtask);
        assertEquals(savedSubtask, subtask);
        assertTrue(epic.getSubtasksId().contains(savedSubtask.getId()));
    }

    @Test
    void updateTask() {
        Task task = new Task("Задача 1", "Описание задачи 1", Status.NEW);

        manager.createTask(task);
        task.setStatus(Status.IN_PROGRESS);
        manager.updateTask(task);

        Task updatedTask = manager.getTaskById(task.getId());
        assertEquals(Status.IN_PROGRESS, updatedTask.getStatus());
    }

    @Test
    void updateEpic() {
        Epic epic = new Epic("Эпик 1", "Описание эпика 1");
        manager.createEpic(epic);
        epic.setDescription("Описание эпика 1 обновленное");
        manager.updateEpic(epic);

        Epic updatedEpic = manager.getEpicById(epic.getId());
        assertEquals("Описание эпика 1 обновленное", updatedEpic.getDescription());
    }

    @Test
    void updateSubtask() {
        Epic epic = new Epic("Эпик 1", "Описание эпика 1");
        manager.createEpic(epic);
        Subtask subtask = new Subtask("Подзадача 1", "Описание подзадачи 1", Status.NEW, epic.getId());
        manager.createSubtask(subtask);
        subtask.setStatus(Status.DONE);
        manager.updateSubtask(subtask);
        Subtask updatedSubtask = manager.getSubtaskById(subtask.getId());

        assertEquals(Status.DONE, updatedSubtask.getStatus());
        assertEquals(Status.DONE, epic.getStatus());
    }

    @Test
    void getEpicSubtasks() {
        Epic epic = new Epic("Эпик 1", "Описание эпика 1");
        manager.createEpic(epic);
        Subtask subtask1 = new Subtask("Подзадача 1", "Описание подзадачи 1", Status.NEW, epic.getId());
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание подзадачи 2", Status.NEW, epic.getId());
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        ArrayList<Subtask> actualSubtasks = new ArrayList<>();
        actualSubtasks.add(subtask1);
        actualSubtasks.add(subtask2);

        assertEquals(actualSubtasks, manager.getEpicSubtasks(epic.getId()));

    }

    @Test
    void deleteTaskById() {
        Task task = new Task("Задача 1", "Описание задачи 1", Status.NEW);

        manager.createTask(task);

        Task savedTask = new Task(task.getId(), task.getName(), task.getDescription(), task.getStatus());

        assertTrue(manager.getAllTasks().contains(savedTask));

        manager.deleteTaskById(task.getId());

        assertFalse(manager.getAllTasks().contains(savedTask));
    }

    @Test
    void deleteEpicById() {
        Epic epic = new Epic("Эпик 1", "Описание эпика 1");
        manager.createEpic(epic);
        Subtask subtask1 = new Subtask("Подзадача 1", "Описание подзадачи 1", Status.NEW, epic.getId());
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание подзадачи 2", Status.NEW, epic.getId());
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        assertNotNull(manager.getEpicById(epic.getId()));
        assertTrue(manager.getEpicById(epic.getId()).getSubtasksId().contains(subtask1.getId()));
        assertTrue(manager.getEpicById(epic.getId()).getSubtasksId().contains(subtask2.getId()));
        assertTrue(manager.getAllSubtasks().contains(subtask1));
        assertTrue(manager.getAllSubtasks().contains(subtask2));

        manager.deleteEpicById(epic.getId());

        assertTrue(manager.getAllEpics().isEmpty());
        assertTrue(manager.getAllSubtasks().isEmpty());
    }

    @Test
    void deleteSubtaskById() {
        Epic epic = new Epic("Эпик 1", "Описание эпика 1");
        manager.createEpic(epic);
        Subtask subtask1 = new Subtask("Подзадача 1", "Описание подзадачи 1", Status.IN_PROGRESS, epic.getId());
        manager.createSubtask(subtask1);

        assertTrue(manager.getEpicById(epic.getId()).getSubtasksId().contains(subtask1.getId()));
        assertTrue(manager.getAllSubtasks().contains(subtask1));
        assertEquals(Status.IN_PROGRESS, epic.getStatus());

        manager.deleteSubtaskById(subtask1.getId());

        assertTrue(manager.getAllSubtasks().isEmpty());
        assertEquals(Status.NEW, epic.getStatus());
        assertTrue(manager.getEpicSubtasks(epic.getId()).isEmpty());
    }

    @Test
    void getAndDeleteAllTasks() {
        Task task1 = new Task("Задача 1", "Описание задачи 1", Status.NEW);
        Task task2 = new Task("Задача 2", "Описание задачи 2", Status.NEW);

        manager.createTask(task1);
        manager.createTask(task2);

        assertTrue(manager.getAllTasks().contains(task1));
        assertTrue(manager.getAllTasks().contains(task2));

        manager.deleteAllTasks();

        assertTrue(manager.getAllTasks().isEmpty());
    }

    @Test
    void getAndDeleteAllEpics() {
        Epic epic1 = new Epic("Эпик 1", "Описание эпика 1");
        Epic epic2 = new Epic("Эпик 2", "Описание эпика 2");
        manager.createEpic(epic1);
        manager.createEpic(epic2);

        assertTrue(manager.getAllEpics().contains(epic1));
        assertTrue(manager.getAllEpics().contains(epic2));


        manager.deleteAllEpics();

        assertTrue(manager.getAllEpics().isEmpty());
    }

    @Test
    void getAndDeleteAllSubtasks() {
        Epic epic = new Epic("Эпик 1", "Описание эпика 1");
        manager.createEpic(epic);
        Subtask subtask1 = new Subtask("Подзадача 1", "Описание подзадачи 1", Status.DONE, epic.getId());
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание подзадачи 2", Status.DONE, epic.getId());
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        assertEquals(Status.DONE, epic.getStatus());
        assertTrue(manager.getAllSubtasks().contains(subtask1));
        assertTrue(manager.getAllSubtasks().contains(subtask2));

        manager.deleteAllSubtasks();

        assertTrue(manager.getAllSubtasks().isEmpty());
        assertEquals(Status.NEW, epic.getStatus());
    }

    @Test
    void getHistory() {
        Epic epic = new Epic("Эпик 1", "Описание эпика 1");
        manager.createEpic(epic);
        Subtask subtask1 = new Subtask("Подзадача 1", "Описание подзадачи 1", Status.NEW, epic.getId());
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание подзадачи 2", Status.NEW, epic.getId());
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        manager.getEpicById(epic.getId());          //1
        manager.getSubtaskById(subtask1.getId());   //2
        manager.getSubtaskById(subtask2.getId());   //3
        manager.getSubtaskById(subtask1.getId());
        manager.getSubtaskById(subtask1.getId());
        manager.getEpicById(epic.getId());

        assertEquals(manager.getHistory().get(2).getId(), epic.getId());
        assertEquals(manager.getHistory().get(0).getId(), subtask2.getId());
    }

    @Test
    void checkHistoryAfterUpdatingTask() {
        Epic epic = new Epic("Эпик 1", "Описание эпика 1");
        manager.createEpic(epic);
        Subtask subtask1 = new Subtask("Подзадача 1", "Описание подзадачи 1", Status.NEW, epic.getId());
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание подзадачи 2", Status.NEW, epic.getId());
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        manager.getEpicById(epic.getId());          //1
        manager.getSubtaskById(subtask1.getId());   //2
        manager.getSubtaskById(subtask2.getId());   //3

        assertEquals(manager.getHistory().get(2).getStatus(), subtask2.getStatus());

        subtask2.setStatus(Status.IN_PROGRESS);
        manager.updateSubtask(subtask2);

        assertEquals(Status.NEW, manager.getHistory().get(2).getStatus());
    }

    @Test
    void checkHistoryAfterDeleteEpic() {
        Epic epic = new Epic("Эпик 1", "Описание эпика 1");
        manager.createEpic(epic);
        Subtask subtask1 = new Subtask("Подзадача 1", "Описание подзадачи 1", Status.NEW, epic.getId());
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание подзадачи 2", Status.NEW, epic.getId());
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        manager.getEpicById(epic.getId());
        manager.getSubtaskById(subtask1.getId());
        manager.getSubtaskById(subtask2.getId());

        manager.deleteAllEpics();
        assertTrue(manager.getHistory().isEmpty());
    }

    @Test
    void checkTimeCrossTasks() {
        Duration duration = Duration.of(15, ChronoUnit.MINUTES);
        Instant startTime1 = Instant.parse("2025-03-03T12:00:00Z");
        Instant startTime2 = Instant.parse("2025-03-03T11:55:00Z");

        Task task1 = new Task("Task1", "Description", Status.NEW, duration, startTime1);
        Task task2 = new Task("Task2", "Description", Status.NEW, duration, startTime2);
        manager.createTask(task1);
        manager.createTask(task2);

        assertEquals(new Task(task1.getId(), "Task1", "Description", Status.NEW, duration, startTime1),
                manager.getPrioritizedTasks().getFirst());
        assertEquals(1, manager.getPrioritizedTasks().size());
    }
}
