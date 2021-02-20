package io.imast.work4j.model.instance;

import java.util.Date;
import lombok.Builder;
import lombok.Data;

/**
 * The job instance model
 * 
 * @author davitp
 */
@Data
@Builder(toBuilder = true)
public class JobInstance {
    
    /**
     * The unique id for single job instance 
     */
    private String id;
    
    /**
     * The identifier of job definition
     */
    private String jobId;
    
    /**
     * The time scheduled as next iteration
     */
    private Date nextIteration;
    
    /**
     * The status of execution
     */
    private JobStatus jobStatus;
    
    /**
     * The completion severity
     */
    private CompletionSeverity completionSeverity;
    
    /**
     * The execution instance was created
     */
    private Date created;
}
