package io.imast.work4j.model;

/**
 * The schedule type
 * 
 * @author davitp
 */
public enum TriggerType {
    
    /**
     * The static period schedule
     */
    STATIC_PERIOD,
    
    /**
     * The Cron type of schedule
     */
    CRON,
    
    /**
     * One-time execution job
     */
    ONE_TIME    
}
