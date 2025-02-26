package tasks;

import java.util.ArrayList;

public class Epic extends Task {
    private final ArrayList<Integer> subtasksId = new ArrayList<>();

    public Epic(String name, String description) {
        super(name, description, Status.NEW);
    }

    public Epic(Integer id, String name, String description, Status status) {
        super(id, name, description, status);
    }

    @Override
    public String toString() {
        return id + "," + TaskTypes.EPIC + "," + name +
                "," + status + "," + description + ",";
    }

    public void updateSubtasksId(Integer id) {
        subtasksId.add(id);
    }

    public ArrayList<Integer> getSubtasksId() {
        return new ArrayList<>(subtasksId);
    }

    public void deleteSubtaskId(Integer id) {
        subtasksId.remove(id);
    }

    public void clearSubtasksId() {
        subtasksId.clear();
    }
}
