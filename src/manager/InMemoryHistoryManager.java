package manager;

import tasks.Task;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {
    private final Map<Integer, Node> history = new HashMap<>();
    private Node head;
    private Node tail;
    private int size = 0;

    @Override
    public void add(Task task) {
        if (history.isEmpty()) {
            linkFirst(task);
        } else if (history.containsKey(task.getId()) && size == 1) {
            removeNode(history.get(task.getId()));
            linkFirst(task);
        } else if (history.containsKey(task.getId())) {
            removeNode(history.get(task.getId()));
            linkLast(task);
        } else {
            linkLast(task);
        }
    }

    private ArrayList<Task> getTasks() {
        ArrayList<Task> historyList = new ArrayList<>();
        for (Node node = head; node != null; node = node.next)
            historyList.add(node.task);
        return historyList;
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    @Override
    public void remove(int id) {
        if (!history.isEmpty() && history.containsKey(id)) {
            removeNode(history.get(id));
            history.remove(id);
        }
    }

    private void removeNode(Node node) {
        if (node == head && node != tail) {
            Node newHead = node.next;
            head = newHead;
            newHead.previous = null;
            history.remove(node.task.getId());
            size--;
        } else if (node != head && node == tail) {
            Node newTail = node.previous;
            tail = newTail;
            newTail.next = null;
            history.remove(node.task.getId());
            size--;
        } else if (node == head) {
            head = null;
            tail = null;
            history.remove(node.task.getId());
            size--;
        } else {
            Node prev = node.previous;
            Node next = node.next;
            prev.next = next;
            next.previous = prev;
            history.remove(node.task.getId());
            size--;
        }
    }

    private void linkLast(Task task) {
        final Node oldTail = tail;
        final Node newNode = new Node(null, oldTail,
                new Task(task.getId(), task.getName(), task.getDescription(), task.getStatus()));
        tail = newNode;
        oldTail.next = newNode;
        size++;
        history.put(task.getId(), newNode);
    }

    private void linkFirst(Task task) {
        final Node newNode = new Node(null, null,
                new Task(task.getId(), task.getName(), task.getDescription(), task.getStatus()));
        tail = newNode;
        head = newNode;
        size++;
        history.put(task.getId(), newNode);
    }

    private static class Node {
        Node next;
        Node previous;
        Task task;

        public Node(Node next, Node previous, Task task) {
            this.next = next;
            this.previous = previous;
            this.task = task;
        }
    }

}
