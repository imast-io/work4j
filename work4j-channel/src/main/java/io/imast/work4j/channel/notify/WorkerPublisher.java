package io.imast.work4j.channel.notify;

import io.imast.work4j.channel.WorkerUpdateMessage;

/**
 * Publishes whatever event worker is interested in
 * 
 * @author davitp
 */
public interface WorkerPublisher {
    
    /**
     * Publishes worker update message
     * 
     * @param message The message to publish
     */
    public void publish(WorkerUpdateMessage message);
}
