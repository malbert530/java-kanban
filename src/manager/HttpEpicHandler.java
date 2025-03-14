package manager;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import exception.*;
import tasks.Epic;
import tasks.Subtask;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class HttpEpicHandler extends BaseHttpHandler {
    private TaskManager manager;
    private Gson jsonMapper;

    public HttpEpicHandler(TaskManager manager, Gson jsonMapper) {
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
        } catch (TaskIdFormatException | BadRequestException e) {
            int code = 400;
            sendErrorResponse(exchange, e.getMessage(), code);
        } catch (ManagerSaveException | ManagerLoadFileException e) {
            int code = 507;
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
            List<Epic> allEpics = manager.getAllEpics();
            String jsonEpics = jsonMapper.toJson(allEpics);
            sendText(exchange, jsonEpics, 200);
        }
        if (splitedPath.length == 3) {
            try {
                int id = Integer.parseInt(splitedPath[2]);
                Epic epicById = manager.getEpicById(id);
                String jsonEpic = jsonMapper.toJson(epicById);
                sendText(exchange, jsonEpic, 200);
            } catch (NumberFormatException e) {
                String errorMessage = String.format("Неверный формат id - %s. Id эпика должен быть числом.", splitedPath[2]);
                throw new TaskIdFormatException(errorMessage);
            }
        }
        if (splitedPath.length == 4) {
            if (splitedPath[3].equals("subtasks")) {
                try {
                    int id = Integer.parseInt(splitedPath[2]);
                    List<Subtask> epicSubtasks = manager.getEpicSubtasks(id);
                    String jsonSubtasks = jsonMapper.toJson(epicSubtasks);
                    sendText(exchange, jsonSubtasks, 200);
                } catch (NumberFormatException e) {
                    String errorMessage = String.format("Неверный формат id - %s. Id эпика должен быть числом.", splitedPath[2]);
                    throw new TaskIdFormatException(errorMessage);
                }
            } else {
                String errorMessage = String.format("Неизвестный запрос - %s", splitedPath[3]);
                throw new BadRequestException(errorMessage);
            }
        }

    }

    private void handlePost(HttpExchange exchange) throws IOException {
        byte[] bytesBody = exchange.getRequestBody().readAllBytes();
        String stringBody = new String(bytesBody, StandardCharsets.UTF_8);
        Epic epic = jsonMapper.fromJson(stringBody, Epic.class);
        if (epic.getId() == null) {
            Epic createdEpic = manager.createEpic(epic);
            String jsonResponse = String.format("Эпик успешно создан с id %d", createdEpic.getId());
            sendText(exchange, jsonResponse, 201);
        } else {
            if (manager.updateEpic(epic)) {
                sendText(exchange, "Эпик успешно обновлен", 201);
            } else {
                throw new TaskHasInteractionsException("Эпик пересекается с уже существующими");
            }
        }

    }

    private void handleDelete(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String[] splitedPath = path.split("/");
        if (splitedPath.length == 3) {
            try {
                int id = Integer.parseInt(splitedPath[2]);
                manager.deleteEpicById(id);
                String deleteMessage = String.format("Эпик с id %d успешно удален", id);
                sendText(exchange, deleteMessage, 200);
            } catch (NumberFormatException e) {
                String errorMessage = String.format("Неверный формат id - %s. Id эпика должен быть числом.", splitedPath[2]);
                throw new TaskIdFormatException(errorMessage);
            }
        }
    }
}
