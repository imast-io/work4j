package io.imast.work4j.model;

/**
 * The trigger type indicates how job should be executed
 * 
 * @author davitp
 */
public enum TriggerType {
    
    /**
     * The trigger type indicates repeating job at fixed period
     */
    PERIODIC,
    
    /**
     * The trigger type indicates schedule of job based on Cron expression
     */
    CRON,
    
    /**
     * The trigger type indicates scheduling job to run once
     */
    ONCE    
}
