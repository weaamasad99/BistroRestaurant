package common;

/**
 * Wrapper class for network transmission using Kryo.
 * Encapsulates a task type and an optional data object (payload).
 * @author Group 6
 * @version 1.0
 */
public class Message {
    /** The type of task to be performed. */
    private TaskType task;
    
    /** The payload data associated with the message. */
    private Object object;

    /**
     * No-arg constructor required for Kryo serialization.
     */
    public Message() {}

    /**
     * Constructs a message with a specific task and payload.
     * @param task The TaskType enum value.
     * @param object The data object (can be null).
     */
    public Message(TaskType task, Object object) {
        this.task = task;
        this.object = object;
    }

    /**
     * Gets the task type.
     * @return The TaskType.
     */
    public TaskType getTask() { return task; }
    
    /**
     * Sets the task type.
     * @param task The TaskType to set.
     */
    public void setTask(TaskType task) { this.task = task; }
    
    /**
     * Gets the payload object.
     * @return The object payload.
     */
    public Object getObject() { return object; }
    
    /**
     * Sets the payload object.
     * @param object The object to set.
     */
    public void setObject(Object object) { this.object = object; }
}