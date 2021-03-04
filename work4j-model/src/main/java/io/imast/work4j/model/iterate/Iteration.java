package io.imast.work4j.model.iterate;

import java.util.Date;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The job iteration data structure
 * 
 * @author davitp
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class Iteration {
    
    /**
     * The iteration identifier
     */
    private String id;
    
    /**
     * The job identifier
     */
    private String jobId;
    
    /**
     * The execution instance identifier
     */
    private String executionId;
    
    /**
     * The worker the iteration was executed in
     */
    private String worker;
    
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
