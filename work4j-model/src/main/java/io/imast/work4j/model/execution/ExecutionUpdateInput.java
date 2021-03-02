package io.imast.work4j.model.execution;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The execution update input
 * 
 * @author davitp
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
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
