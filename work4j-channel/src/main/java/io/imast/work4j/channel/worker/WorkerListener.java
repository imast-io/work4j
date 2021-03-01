package io.imast.work4j.channel.worker;

import java.util.function.Consumer;

/**
 * The worker events listener
 * 
 * @author davitp
 */
public interface WorkerListener {
    
    /**
     * Starts the listener
     */
    public void start();

    /**
     * Add a given consumer
     * 
     * @param consumer The consumer to add
     */
    public void add(Consumer<WorkerMessage> consumer);

    /**
     * Remove the given consumer
     * 
     * @param consumer The consumer to remove
     */
    public void remove(Consumer<WorkerMessage> consumer);

    /**
     * Stop the listener
     */
    public void stop();
}
