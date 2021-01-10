package io.imast.work4j.model;

import java.io.Serializable;

/**
 * The status of job
 * 
 * @author davitp
 */
public enum JobStatus implements Serializable {
    
    /**
     * The Job is defined but not submitted for execution
     */
    DEFINED, 
    
    /**
     * Job is created and definition is stored to database
     */
    ACTIVE,
    
    /**
     * The execution is paused 
     */
    PAUSED,
    
    /**
     * The job is assigned and agent failed to execute job
     */
    FAILED,
    
    /**
     * Job is completed and has no more things to do
     */
    COMPLETED
}
