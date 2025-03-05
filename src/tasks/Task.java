package tasks;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;


public class Task {
    protected Integer id;
    protected String name;
    protected String description;
    protected Status status;
    protected Duration duration;
    protected Instant startTime;

    public Task(Integer id, String name, String description, Status status) {
        this(name, description, status);
        this.id = id;
    }

    public Task(String name, String description, Status status) {
        this.name = name;
        this.description = description;
        this.status = status;
        this.duration = null;
        this.startTime = null;
    }

    public Task(Integer id, String name, String description, Status status, Duration duration, Instant startTime) {
        this(name, description, status, duration, startTime);
        this.id = id;
    }

    public Task(String name, String description, Status status, Duration duration, Instant startTime) {
        this.name = name;
        this.description = description;
        this.status = status;
        this.duration = duration;
        this.startTime = startTime;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Instant getEndTime() {
        return startTime == null ? null : startTime.plus(duration.toMinutes(), ChronoUnit.MINUTES);
    }

    public Instant getStartTime() {
        return startTime;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        Task task = (Task) o;
        return Objects.equals(id, task.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return id + "," + TaskTypes.TASK + "," + name +
                "," + status + "," + description + "," + startTime + ","
                + (duration == null ? null : duration.toMinutes()) + ",";
    }
}
