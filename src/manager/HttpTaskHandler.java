package manager;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import exception.*;
import tasks.Task;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class HttpTaskHandler extends BaseHttpHandler {
    private TaskManager manager;
    private Gson jsonMapper;

    public HttpTaskHandler(TaskManager manager, Gson jsonMapper) {
        this.manager = manager;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        try {
            switch (method) {
                case "GET":
                    handleGet(exchange);
                    break;
                case "POST":
                    handlePost(exchange);
                    break;
                case "DELETE":
                    handleDelete(exchange);
                    break;
                default:
                    int code = 405;
                    String message = String.format("Обработка метода %s не предусмотрена", method);
                    sendErrorResponse(exchange, message, code);
            }
        } catch (TaskNotFoundException e) {
            int code = 404;
            sendErrorResponse(exchange, e.getMessage(), code);
        } catch (TaskIdFormatException e) {
            int code = 400;
            sendErrorResponse(exchange, e.getMessage(), code);
        } catch (ManagerSaveException | ManagerLoadFileException e) {
            int code = 507;
            sendErrorResponse(exchange, e.getMessage(), code);
        } catch (TaskHasInteractionsException e) {
            int code = 406;
            sendErrorResponse(exchange, e.getMessage(), code);
        } catch (Exception e) {
            int code = 500;
            sendErrorResponse(exchange, e.getMessage(), code);
        } finally {
            exchange.close();
        }
    }

    private void sendErrorResponse(HttpExchange exchange, String errorMessage, int code) throws IOException {
        ErrorResponse errorResponse = new ErrorResponse(errorMessage, exchange.getRequestURI().getPath(), code);
        String jsonError = jsonMapper.toJson(errorResponse);
        sendText(exchange, jsonError, code);
    }

    private void handleGet(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String[] splitedPath = path.split("/");
        if (splitedPath.length == 2) {
            List<Task> allTasks = manager.getAllTasks();
            String jsonTasks = jsonMapper.toJson(allTasks);
            sendText(exchange, jsonTasks, 200);
        }
        if (splitedPath.length == 3) {
            try {
                int id = Integer.parseInt(splitedPath[2]);
                Task taskById = manager.getTaskById(id);
                String jsonTask = jsonMapper.toJson(taskById);
                sendText(exchange, jsonTask, 200);
            } catch (NumberFormatException e) {
                String errorMessage = String.format("Неверный формат id - %s. Id задачи должен быть числом.", splitedPath[2]);
                throw new TaskIdFormatException(errorMessage);
            }
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        byte[] bytesBody = exchange.getRequestBody().readAllBytes();
        String stringBody = new String(bytesBody, StandardCharsets.UTF_8);
        Task task = jsonMapper.fromJson(stringBody, Task.class);
        if (task.getId() == null) {
            Task createdTask = manager.createTask(task);
            if (createdTask.getId() == null) {
                throw new TaskHasInteractionsException("Задача пересекается с уже существующими");
            }
            String jsonResponse = String.format("Задача успешно создана с id %d", createdTask.getId());
            sendText(exchange, jsonResponse, 201);
        } else {
            if (manager.updateTask(task)) {
                sendText(exchange, "Задача успешно обновлена", 201);
            } else {
                throw new TaskHasInteractionsException("Задача пересекается с уже существующими");
            }
        }

    }

    private void handleDelete(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String[] splitedPath = path.split("/");
        if (splitedPath.length == 3) {
            try {
                int id = Integer.parseInt(splitedPath[2]);
                manager.deleteTaskById(id);
                String deleteMessage = String.format("Задача с id %d успешно удалена", id);
                sendText(exchange, deleteMessage, 200);
            } catch (NumberFormatException e) {
                String errorMessage = String.format("Неверный формат id - %s. Id задачи должен быть числом.", splitedPath[2]);
                throw new TaskIdFormatException(errorMessage);
            }
        }
    }
}
