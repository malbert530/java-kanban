import manager.*;
import tasks.*;

import java.io.File;

public class Main {

    public static void main(String[] args) {
        //      FileBackedTaskManager manager = FileBackedTaskManager.loadFromFile(new File("data.csv"));
//        System.out.println(manager.getAllSubtasks().toString());
//        System.out.println(manager.getAllEpics().toString());
//        System.out.println(manager.getEpicById(2).getSubtasksId());

        FileBackedTaskManager manager = new FileBackedTaskManager(new File("data.csv"));
//
//
        Task task1 = new Task(null, "Homework1", "Do my homework1", Status.NEW);
        Task task2 = new Task(null, "Homework2", "Do my homework2", Status.NEW);
        manager.createTask(task1);
        manager.createTask(task2);

        System.out.println(manager.getAllTasks().toString());
        manager.getTaskById(0);
        manager.getTaskById(1);
        manager.getTaskById(0);
        System.out.println(manager.getHistory());

        Epic epic1 = new Epic("Epic Homework3", "Do my homework3");
        Epic epic2 = new Epic("Epic Hobby time", "Listen to music");
        manager.createEpic(epic1);
        manager.createEpic(epic2);

        Subtask subtask1 = new Subtask("Read book", "Read War and Peace", Status.NEW, epic1.getId());
        Subtask subtask2 = new Subtask("Write poem", "Write poem like Pushkin", Status.NEW, epic1.getId());
        Subtask subtask3 = new Subtask("Enjoy music", "Listening Linkin Park", Status.NEW, epic2.getId());
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);
        manager.createSubtask(subtask3);

    }
}
