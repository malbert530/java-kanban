package tasks;

import java.time.Duration;
import java.time.Instant;

public class Subtask extends Task {
    private Integer epicId;

    public Subtask(String name, String description, Status status, Integer epicId) {
        super(name, description, status);
        this.epicId = epicId;
    }

    public Subtask(Integer id, String name, String description, Status status, Integer epicId) {
        super(id, name, description, status);
        this.epicId = epicId;
    }

    public Subtask(Integer id, String name, String description, Status status, Duration duration, Instant startTime, Integer epicId) {
        super(id, name, description, status, duration, startTime);
        this.epicId = epicId;
    }

    public Subtask(String name, String description, Status status, Duration duration, Instant startTime, Integer epicId) {
        super(name, description, status, duration, startTime);
        this.epicId = epicId;
    }

    public Integer getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return id + "," + TaskTypes.SUBTASK + "," + name +
                "," + status + "," + description + "," + startTime + ","
                + (duration == null ? null : duration.toMinutes()) + "," + epicId;
    }
}
