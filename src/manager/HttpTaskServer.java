package manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.Instant;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private TaskManager manager;
    private HttpServer httpServer;

    public HttpTaskServer(TaskManager manager) {
        this.manager = manager;
    }

    public void start() throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
        Gson jsonMapper = getGson();
        httpServer.createContext("/tasks", new HttpTaskHandler(manager, jsonMapper));
        httpServer.createContext("/epics", new HttpEpicHandler(manager, jsonMapper));
        httpServer.createContext("/subtasks", new HttpSubtaskHandler(manager, jsonMapper));
        httpServer.createContext("/history", new HttpHistoryHandler(manager, jsonMapper));
        httpServer.createContext("/prioritized", new HttpPrioritizedHandler(manager, jsonMapper));
        httpServer.start();
    }

    public void stop(int delay) {
        httpServer.stop(delay);
    }

    public static Gson getGson() {
        Gson jsonMapper = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(Instant.class, new InstantAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .create();
        return jsonMapper;
    }

    public static void main(String[] args) throws IOException {
        HttpTaskServer server = new HttpTaskServer(Managers.getDefault());
        server.start();
    }

}
