package io.imast.work4j.model.cluster;

import java.io.Serializable;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The cluster worker instance
 * 
 * @author davitp
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class ClusterWorker implements Serializable {
    
    /**
     * The cluster worker name
     */
    private String name;
    
    /**
     * The cluster identity
     */
    private String cluster;
    
    /**
     * The worker kind within cluster
     */
    private WorkerKind kind;
 
    /**
     * The session token to differentiate
     */
    private String session;
    
    /**
     * Checks if persistence is enabled
     */
    private boolean persistence;
    
    /**
     * The persistence method value for reference only
     */
    private String persistenceMethod;
    
    /**
     * The maximum idle time
     */
    private long maxIdle;
    
    /**
     * The last check-in activity
     */
    private WorkerActivity activity;

    /**
     * The update time of worker
     */
    private Date updated;
}