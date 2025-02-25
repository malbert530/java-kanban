package tasks;

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

    public Integer getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return id + "," + TaskTypes.SUBTASK + "," + name +
                "," + status + "," + description + "," + epicId;
    }
}
