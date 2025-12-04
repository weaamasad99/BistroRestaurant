package common;

/**
 * Wrapper class for network transmission using Kryo.
 */
public class Message {
    private TaskType task;
    private Object object;

    // No-arg constructor for Kryo
    public Message() {}

    public Message(TaskType task, Object object) {
        this.task = task;
        this.object = object;
    }

    public TaskType getTask() { return task; }
    public void setTask(TaskType task) { this.task = task; }
    public Object getObject() { return object; }
    public void setObject(Object object) { this.object = object; }
}