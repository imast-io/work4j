package io.imast.work4j.model.execution;

import lombok.Builder;
import lombok.Data;

/**
 * The execution update input
 * 
 * @author davitp
 */
@Data
@Builder(toBuilder = true)
public class ExecutionUpdateInput {
    
    /**
     * The execution status
     */
    private ExecutionStatus status;
    
    /**
     * The completion severity
     */
    private CompletionSeverity severity;
}
