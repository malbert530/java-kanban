package exception;

public class TaskIdFormatException extends RuntimeException {
    public TaskIdFormatException(String message) {
        super(message);
    }
}
