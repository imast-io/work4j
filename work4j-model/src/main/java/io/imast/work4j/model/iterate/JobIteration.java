package io.imast.work4j.model.iterate;

import java.time.ZonedDateTime;
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
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JobIteration {
    
    /**
     * The entry id
     */
    private String id;
    
    /**
     * The job id
     */
    private String jobId;
    
    /**
     * The job iteration status
     */
    private IterationStatus status;
    
    /**
     * The message
     */
    private String message;
    
    /**
     * The iteration payload
     */
    private Map<String, Object> payload;
    
    /**
     * The run time of iteration
     */
    private Long runtime;
    
    /**
     * The timestamp
     */
    private ZonedDateTime timestamp;
}
