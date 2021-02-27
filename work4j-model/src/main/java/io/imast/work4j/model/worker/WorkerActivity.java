package io.imast.work4j.model.worker;

/**
 * The types of worker activities
 * 
 * @author davitp
 */
public enum WorkerActivity {
    
    /**
     * The worker registration activity
     */
    REGISTER,
    
    /**
     *
     * Worker heartbeat indicates health of worker
     */
    HEARTBEAT,
    
    /**
     * Worker sent the signal about shutting down
     */
    SHUTDOWN
}
