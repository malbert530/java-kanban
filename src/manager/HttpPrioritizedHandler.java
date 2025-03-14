package manager;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import exception.*;
import tasks.Task;

import java.io.IOException;
import java.util.List;

public class HttpPrioritizedHandler extends BaseHttpHandler {
    private TaskManager manager;
    private Gson jsonMapper;

    public HttpPrioritizedHandler(TaskManager manager, Gson jsonMapper) {
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
                default:
                    int code = 405;
                    String message = String.format("Обработка метода %s не предусмотрена", method);
                    sendErrorResponse(exchange, message, code);
            }
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
            List<Task> prioritizedTasks = manager.getPrioritizedTasks();
            String jsonPrioritized = jsonMapper.toJson(prioritizedTasks);
            sendText(exchange, jsonPrioritized, 200);
        }
    }
}