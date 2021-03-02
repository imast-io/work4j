package io.imast.work4j.model.execution;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Execution status response entry
 * 
 * @author davitp
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class ExecutionIndexEntry {
    
    /**
     * The execution id
     */
    private String id;
    
    /**
     * The job identifier
     */
    private String jobId;
    
    /**
     * The execution status
     */
    private ExecutionStatus status;
}
