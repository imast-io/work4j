package io.imast.work4j.model.cluster;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The input for joining the cluster
 * 
 * @author davitp
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class ClusterJoinInput implements Serializable {
    
    /**
     * The tenant reference
     */
    private String tenant;
    
    /**
     * The cluster reference
     */
    private String cluster;
    
    /**
     * The worker name to join
     */
    private String worker;
    
    /**
     * The session of worker
     */
    private String session;
    
    /**
     * The joining worker kind
     */
    private WorkerKind kind;
    
    /**
     * The maximum idle time in milliseconds
     */
    private long maxIdle;
}
