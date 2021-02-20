package io.imast.work4j.model.instance;

import java.io.Serializable;

/**
 * The status of job
 * 
 * @author davitp
 */
public enum JobStatus implements Serializable {
    
    /**
     * Job is created and definition is stored to database
     */
    ACTIVE,
    
    /**
     * Job is completed and has no more things to do
     */
    COMPLETED
}
