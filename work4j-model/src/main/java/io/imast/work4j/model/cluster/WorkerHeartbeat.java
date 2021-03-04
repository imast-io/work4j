package io.imast.work4j.model.cluster;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The worker heartbeat definition
 * 
 * @author davitp
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class WorkerHeartbeat {
    
    /**
     * The cluster name
     */
    private String cluster;
    
    /**
     * The worker name
     */
    private String name;
    
    /**
     * The worker latest activity
     */
    private WorkerActivity activity;
}
