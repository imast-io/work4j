package io.imast.work4j.model.worker;

import java.util.Date;
import lombok.Builder;
import lombok.Data;

/**
 * The worker information
 * 
 * @author davitp
 */
@Data
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
    private String worker;
    
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
