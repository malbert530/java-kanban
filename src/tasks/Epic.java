package tasks;

import java.util.ArrayList;

public class Epic extends Task {
    private final ArrayList<Integer> subtasksId = new ArrayList<>();

    public Epic(String name, String description) {
        super(name, description, Status.NEW);
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", subtasksId=" + subtasksId +
                '}';
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
