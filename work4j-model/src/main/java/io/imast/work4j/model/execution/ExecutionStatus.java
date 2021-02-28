package io.imast.work4j.model.execution;

import java.io.Serializable;

/**
 * The status of job execution 
 * 
 * @author davitp
 */
public enum ExecutionStatus implements Serializable {
    
    /**
     * Job instance is in active state
     */
    ACTIVE,
    
    /**
     * The job execution is paused
     */
    PAUSED,
    
    /**
     * Job is completed and has no more things to do
     */
    COMPLETED
}
