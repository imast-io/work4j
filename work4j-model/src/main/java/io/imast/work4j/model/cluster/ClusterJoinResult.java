package io.imast.work4j.model.cluster;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The result of joining the cluster
 * 
 * @author davitp
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class ClusterJoinResult implements Serializable {
    
    /**
     * The identifier of cluster 
     */
    private String id;
    
    /**
     * The name of cluster
     */
    private String cluster;
    
    /**
     * The effective name of worker
     */
    private String worker;
    
    /**
     * The maximum idle time for the given 
     */
    private long maxIdle;
    
    /**
     * The flag to identify if join request was successful 
     */
    private boolean joined;
    
}
