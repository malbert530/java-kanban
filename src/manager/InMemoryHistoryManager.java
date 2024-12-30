package manager;

import tasks.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private final ArrayList<Task> history = new ArrayList<>(10);

    @Override
    public void add(Task task) {
        if (history.size() == 10) {
            history.remove(0);
        }
        history.add(new Task(task.getId(), task.getName(), task.getDescription(), task.getStatus()));
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(history);
    }
}
