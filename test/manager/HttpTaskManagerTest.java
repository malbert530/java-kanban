package manager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import tasks.Epic;
import tasks.Status;
import tasks.Subtask;
import tasks.Task;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskListTypeToken extends TypeToken<List<Task>> {
}

class SubtaskListTypeToken extends TypeToken<List<Subtask>> {
}

class EpicListTypeToken extends TypeToken<List<Epic>> {
}

public class HttpTaskManagerTest {
    private static final int PORT = 8080;
    TaskManager manager = Managers.getDefault();
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    Gson jsonMapper = HttpTaskServer.getGson();


    public HttpTaskManagerTest() throws IOException {
    }

    @BeforeEach
    public void setUp() throws IOException {
        manager.deleteAllTasks();
        manager.deleteAllEpics();
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop(1);
    }

    @Test
    public void testAddTask() throws IOException, InterruptedException {

        Task task = new Task("Test 2", "Testing task 2",
                Status.NEW, Duration.ofMinutes(5), Instant.now());
        String taskJson = jsonMapper.toJson(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Task> tasksFromManager = manager.getAllTasks();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Test 2", tasksFromManager.getFirst().getName(), "Некорректное имя задачи");
    }

    @Test
    public void testUpdateTask() throws IOException, InterruptedException {

        Task task = new Task("Test 2", "Testing task 2",
                Status.NEW, Duration.ofMinutes(5), Instant.now());
        Task createdTask = manager.createTask(task);
        Task updatedTask = new Task(createdTask.getId(), createdTask.getName(),
                "Updated description", createdTask.getStatus(),
                createdTask.getDuration(), createdTask.getStartTime());
        String taskJson = jsonMapper.toJson(updatedTask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        Task taskFromManager = manager.getTaskById(createdTask.getId());

        assertNotNull(taskFromManager, "Задача не возвращается");
        assertEquals("Updated description", taskFromManager.getDescription(), "Некорректное описание задачи");
    }

    @Test
    public void testGetTaskById() throws IOException, InterruptedException {
        Task task = new Task("Test 2", "Testing task 2",
                Status.NEW, Duration.ofMinutes(5), Instant.now());
        Task createdTask = manager.createTask(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/" + createdTask.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        Task responseTask = jsonMapper.fromJson(response.body(), Task.class);

        assertNotNull(responseTask, "Задача не возвращаются");
        assertEquals(createdTask.getId(), responseTask.getId(), "Некорректное id задачи");
        assertEquals("Test 2", responseTask.getName(), "Некорректное имя задачи");
    }

    @Test
    public void testGetAllTasks() throws IOException, InterruptedException {
        Task task1 = new Task("Test 1", "Testing task 1",
                Status.NEW, Duration.ofMinutes(5), Instant.now());
        Task task2 = new Task("Test 2", "Testing task 2",
                Status.NEW);
        manager.createTask(task1);
        manager.createTask(task2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        List<Task> responseTasks = jsonMapper.fromJson(response.body(), new TaskListTypeToken().getType());

        assertNotNull(responseTasks, "Задачи не возвращаются");
        assertEquals(2, responseTasks.size(), "Некорректное количество задач");
        assertEquals("Test 2", responseTasks.getLast().getName(), "Некорректное имя задачи");
    }

    @Test
    public void testDeleteTaskById() throws IOException, InterruptedException {
        Task task = new Task("Test 2", "Testing task 2",
                Status.NEW, Duration.ofMinutes(5), Instant.now());
        Task createdTask = manager.createTask(task);

        assertNotNull(manager.getTaskById(createdTask.getId()));
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/" + createdTask.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        List<Task> listTask = manager.getAllTasks();
        assertTrue(listTask.isEmpty(), "Задача не удаляется");
    }

    @Test
    public void testAddSubtask() throws IOException, InterruptedException {

        Epic epic = new Epic("Test 1", "Description");
        Epic createdEpic = manager.createEpic(epic);
        Subtask subtask = new Subtask("Test 2", "Testing task 2",
                Status.NEW, Duration.ofMinutes(5), Instant.now(), createdEpic.getId());
        String subtaskJson = jsonMapper.toJson(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(subtaskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Subtask> subtasksFromManager = manager.getAllSubtasks();

        assertNotNull(subtasksFromManager, "Задачи не возвращаются");
        assertEquals(1, subtasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Test 2", subtasksFromManager.getFirst().getName(), "Некорректное имя задачи");
    }

    @Test
    public void testUpdateSubtask() throws IOException, InterruptedException {

        Epic epic = new Epic("Test 1", "Description");
        Epic createdEpic = manager.createEpic(epic);
        Subtask subtask = new Subtask("Test 2", "Testing task 2",
                Status.NEW, Duration.ofMinutes(5), Instant.now(), createdEpic.getId());
        Subtask createdSubtask = manager.createSubtask(subtask);

        Subtask updatedSubtask = new Subtask(createdSubtask.getId(), createdSubtask.getName(),
                "Updated description", createdSubtask.getStatus(),
                createdSubtask.getDuration(), createdSubtask.getStartTime(), createdSubtask.getEpicId());
        String subtaskJson = jsonMapper.toJson(updatedSubtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(subtaskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        Task subtaskFromManager = manager.getSubtaskById(createdSubtask.getId());

        assertNotNull(subtaskFromManager, "Задача не возвращается");
        assertEquals("Updated description", subtaskFromManager.getDescription(), "Некорректное описание задачи");
    }

    @Test
    public void testGetSubtaskById() throws IOException, InterruptedException {
        Epic epic = new Epic("Test 1", "Description");
        Epic createdEpic = manager.createEpic(epic);
        Subtask subtask = new Subtask("Test 2", "Testing task 2",
                Status.NEW, Duration.ofMinutes(5), Instant.now(), createdEpic.getId());
        Subtask createdSubtask = manager.createSubtask(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/" + createdSubtask.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        Task responseSubtask = jsonMapper.fromJson(response.body(), Subtask.class);

        assertNotNull(responseSubtask, "Задача не возвращаются");
        assertEquals(createdSubtask.getId(), responseSubtask.getId(), "Некорректное id задачи");
        assertEquals("Test 2", responseSubtask.getName(), "Некорректное имя задачи");
    }

    @Test
    public void testGetAllSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Test 1", "Description");
        Epic createdEpic = manager.createEpic(epic);
        Subtask subtask = new Subtask("Test 2", "Testing task 2",
                Status.NEW, Duration.ofMinutes(5), Instant.now(), createdEpic.getId());
        Subtask subtask1 = new Subtask("Test 3", "Description", Status.NEW, createdEpic.getId());
        manager.createSubtask(subtask);
        manager.createSubtask(subtask1);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        List<Subtask> responseSubtasks = jsonMapper.fromJson(response.body(), new SubtaskListTypeToken().getType());

        assertNotNull(responseSubtasks, "Задачи не возвращаются");
        assertEquals(2, responseSubtasks.size(), "Некорректное количество задач");
        assertEquals("Test 3", responseSubtasks.getLast().getName(), "Некорректное имя задачи");
    }

    @Test
    public void testDeleteSubtaskById() throws IOException, InterruptedException {
        Epic epic = new Epic("Test 1", "Description");
        Epic createdEpic = manager.createEpic(epic);
        Subtask subtask = new Subtask("Test 2", "Testing task 2",
                Status.NEW, Duration.ofMinutes(5), Instant.now(), createdEpic.getId());
        Subtask createdSubtask = manager.createSubtask(subtask);

        assertNotNull(manager.getSubtaskById(createdSubtask.getId()));
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/" + createdSubtask.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        List<Subtask> listSubtask = manager.getAllSubtasks();
        assertTrue(listSubtask.isEmpty(), "Задача не удаляется");
    }

    @Test
    public void testAddEpic() throws IOException, InterruptedException {

        Epic epic = new Epic("Test 1", "Description");
        String epicJson = jsonMapper.toJson(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(epicJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Epic> epicsFromManager = manager.getAllEpics();

        assertNotNull(epicsFromManager, "Задачи не возвращаются");
        assertEquals(1, epicsFromManager.size(), "Некорректное количество задач");
        assertEquals("Test 1", epicsFromManager.getFirst().getName(), "Некорректное имя задачи");
    }

    @Test
    public void testUpdateEpic() throws IOException, InterruptedException {

        Epic epic = new Epic("Test 1", "Description");
        Epic createdEpic = manager.createEpic(epic);

        Epic updatedEpic = new Epic(createdEpic.getId(), createdEpic.getName(), "Updated description", createdEpic.getStatus());
        String epicJson = jsonMapper.toJson(updatedEpic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(epicJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        Epic epicFromManager = manager.getEpicById(createdEpic.getId());

        assertNotNull(epicFromManager, "Задача не возвращается");
        assertEquals("Updated description", epicFromManager.getDescription(), "Некорректное описание задачи");
    }

    @Test
    public void testGetEpicById() throws IOException, InterruptedException {
        Epic epic = new Epic("Test 1", "Description");
        Epic createdEpic = manager.createEpic(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + createdEpic.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        Epic responseEpic = jsonMapper.fromJson(response.body(), Epic.class);

        assertNotNull(responseEpic, "Задача не возвращаются");
        assertEquals(createdEpic.getId(), responseEpic.getId(), "Некорректное id задачи");
        assertEquals("Test 1", responseEpic.getName(), "Некорректное имя задачи");
    }

    @Test
    public void testGetAllEpics() throws IOException, InterruptedException {
        Epic epic = new Epic("Test 1", "Description");
        Epic epic2 = new Epic("Test 2", "Description 2");
        manager.createEpic(epic);
        manager.createEpic(epic2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        List<Epic> responseEpics = jsonMapper.fromJson(response.body(), new EpicListTypeToken().getType());

        assertNotNull(responseEpics, "Задачи не возвращаются");
        assertEquals(2, responseEpics.size(), "Некорректное количество задач");
        assertEquals("Test 2", responseEpics.getLast().getName(), "Некорректное имя задачи");
    }

    @Test
    public void testDeleteEpicById() throws IOException, InterruptedException {
        Epic epic = new Epic("Test 1", "Description");
        Epic createdEpic = manager.createEpic(epic);

        assertNotNull(manager.getEpicById(createdEpic.getId()));
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + createdEpic.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        List<Epic> listSubtask = manager.getAllEpics();
        assertTrue(listSubtask.isEmpty(), "Задача не удаляется");
    }

    @Test
    public void testGetHistory() throws IOException, InterruptedException {
        Task task1 = new Task("Test 2", "Testing task 2",
                Status.NEW, Duration.ofMinutes(5), Instant.now());
        Task task2 = new Task("Test 3", "Testing task 3",
                Status.NEW);
        Task createdTask1 = manager.createTask(task1);
        Task createdTask2 = manager.createTask(task2);
        assertTrue(manager.getHistory().isEmpty());

        manager.getTaskById(createdTask1.getId());
        manager.getTaskById(createdTask2.getId());
        manager.getTaskById(createdTask1.getId());

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        List<Task> responseHistory = jsonMapper.fromJson(response.body(), new TaskListTypeToken().getType());

        assertNotNull(responseHistory, "Задача не возвращаются");
        assertEquals(2, responseHistory.size(), "Некорректное количество задач");
        assertEquals("Test 2", responseHistory.getLast().getName(), "Некорректное имя задачи");
    }

    @Test
    public void testGetPrioritized() throws IOException, InterruptedException {
        Instant start1 = Instant.parse("2025-03-03T13:00:00Z");
        Instant start2 = Instant.parse("2025-03-03T13:30:00Z");
        Instant start3 = Instant.parse("2025-03-03T13:45:00Z");

        Task task1 = new Task("Test 2", "Testing task 2",
                Status.NEW, Duration.ofMinutes(5), start3);
        Task task2 = new Task("Test 3", "Testing task 3",
                Status.NEW, Duration.ofMinutes(10), start1);
        Task task3 = new Task("Test 3", "Testing task 3",
                Status.NEW, Duration.ofMinutes(10), start2);
        manager.createTask(task1);
        manager.createTask(task2);
        manager.createTask(task3);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        List<Task> responsePrioritized = jsonMapper.fromJson(response.body(), new TaskListTypeToken().getType());

        assertNotNull(responsePrioritized, "Задача не возвращаются");
        assertEquals(3, responsePrioritized.size(), "Некорректное количество задач");
        assertEquals("Test 2", responsePrioritized.getLast().getName(), "Некорректное имя задачи");
    }
}