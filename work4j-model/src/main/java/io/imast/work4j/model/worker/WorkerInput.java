package io.imast.work4j.model.worker;

import lombok.Builder;
import lombok.Data;

/**
 * The worker input
 * 
 * @author davitp
 */
@Data
@Builder(toBuilder = true)
public class WorkerInput {
    
    /**
     * The cluster name
     */
    private String cluster;
    
    /**
     * The worker name
     */
    private String worker;
    
    /**
     * The tenant identifier
     */
    private String tenant;
    
    /**
     * The maximum idle time to consider worker session as active
     */
    private Long maxIdle;
}
