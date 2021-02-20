package io.imast.work4j.model.iterate;

import java.util.Date;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

/**
 * The job iteration data structure
 * 
 * @author davitp
 */
@Data
@Builder(toBuilder = true)
public class Iteration {
    
    /**
     * The iteration identifier
     */
    private String id;
    
    /**
     * The job id
     */
    private String jobId;
    
    /**
     * The session the iteration was executed in
     */
    private String session;
    
    /**
     * The status of performed iteration 
     */
    private IterationStatus status;
    
    /**
     * The message of iteration if available
     */
    private String message;
    
    /**
     * The iteration payload if available
     */
    private Map<String, Object> payload;
    
    /**
     * The run time of iteration
     */
    private Long runtime;
    
    /**
     * The timestamp of the iteration
     */
    private Date timestamp;
}
