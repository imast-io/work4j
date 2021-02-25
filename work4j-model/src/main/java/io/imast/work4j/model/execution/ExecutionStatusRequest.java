package io.imast.work4j.model.execution;

import lombok.Builder;
import lombok.Data;

/**
 * The execution status request 
 * 
 * @author davitp
 */
@Data
@Builder(toBuilder = true)
public class ExecutionStatusRequest {
    
    /**
     * The target cluster
     */
    private String cluster;
    
    /**
     * The target tenant
     */
    private String tenant;    
}
