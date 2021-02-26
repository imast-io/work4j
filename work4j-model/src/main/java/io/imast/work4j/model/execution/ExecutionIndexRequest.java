package io.imast.work4j.model.execution;

import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * The execution index request 
 * 
 * @author davitp
 */
@Data
@Builder(toBuilder = true)
public class ExecutionIndexRequest {
    
    /**
     * The target cluster
     */
    private String cluster;
    
    /**
     * The target tenant
     */
    private String tenant;   
    
    /**
     * The specific executions to request
     */
    private List<String> executions;
}
