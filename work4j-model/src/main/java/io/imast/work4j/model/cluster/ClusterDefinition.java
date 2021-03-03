package io.imast.work4j.model.cluster;

import java.io.Serializable;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The cluster definition data model
 * 
 * @author davitp
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class ClusterDefinition implements Serializable {
    
    /**
     * The cluster identifier
     */
    private String id;
    
    /**
     * The tenant of cluster
     */
    private String tenant;
    
    /**
     * The cluster name
     */
    private String cluster;
    
    /**
     * The maximum idle time 
     */
    private long maxIdle;
    
    /**
     * The cluster definition time
     */
    private Date created;
    
    /**
     * The cluster update time
     */
    private Date updated;
}
