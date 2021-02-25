package io.imast.work4j.model.execution;

import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * The execution status request 
 * 
 * @author davitp
 */
@Data
@Builder(toBuilder = true)
public class ExecutionStatusResponse {
    
    /**
     * The target cluster
     */
    private String cluster;
    
    /**
     * The target tenant
     */
    private String tenant;    
    
    /**
     * The map of executions and their ids
     */
    private List<ExecutionStatusResponseEntry> executions;
}
