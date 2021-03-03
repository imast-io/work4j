package io.imast.work4j.model.cluster;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The worker input
 * 
 * @author davitp
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class WorkerInput {
    
    /**
     * The cluster name
     */
    private String cluster;
    
    /**
     * The worker name
     */
    private String name;
    
    /**
     * The tenant identifier
     */
    private String tenant;
    
    /**
     * The maximum idle time to consider worker session as active
     */
    private Long maxIdle;
}
