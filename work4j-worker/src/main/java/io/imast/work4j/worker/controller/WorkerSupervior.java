package io.imast.work4j.worker.controller;

import java.util.function.Consumer;

/**
 * The worker listener
 * 
 * @author davitp
 */
public interface WorkerSupervior {
    
    /**
     * Start listening for updates
     */
    public void start();
    
    /**
     * Adds a consumer function
     * 
     * @param consumer The consumer to register
     */
    public void add(Consumer<WorkerUpdateMessage> consumer);
    
    /**
     * Removes the consumer function
     * 
     * @param consumer The consumer to register
     */
    public void remove(Consumer<WorkerUpdateMessage> consumer);
        
    /**
     * Stop listening for updates
     */
    public void stop();
}
