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

    public static void main(String[] args) throws IOException {
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
        Gson jsonMapper = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(Instant.class, new InstantAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .create();
        TaskManager manager = Managers.getDefault();
        httpServer.createContext("/tasks", new HttpTaskHandler(manager, jsonMapper));
        httpServer.createContext("/epics", new HttpEpicHandler(manager, jsonMapper));
        httpServer.createContext("/subtasks", new HttpSubtaskHandler(manager, jsonMapper));
        httpServer.createContext("/history", new HttpHistoryHandler(manager, jsonMapper));
        httpServer.createContext("/prioritized", new HttpPrioritizedHandler(manager, jsonMapper));
        httpServer.start();
    }
}
