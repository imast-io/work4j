package io.imast.work4j.model.cluster;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The worker information
 * 
 * @author davitp
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class Worker {
    
    /**
     * The worker identifier
     */
    private String id;
    
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
    
    /**
     * The session creation time
     */
    private Date created;
    
    /**
     * The session last update time
     */
    private Date updated;
    
    /**
     * The last activity in the session
     */
    private WorkerActivity activity;
}
