package io.imast.work4j.model.execution;

import lombok.Builder;
import lombok.Data;

/**
 * Execution status response entry
 * 
 * @author davitp
 */
@Data
@Builder(toBuilder = true)
public class ExecutionIndexEntry {
    
    /**
     * The job identifier
     */
    private String jobId;
    
    /**
     * The execution id
     */
    private String executionId;
    
    /**
     * The execution status
     */
    private ExecutionStatus status;
}
