package io.imast.work4j.worker;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The Worker Configuration
 * 
 * @author davitp
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkerConfiguration {
    
    /**
     * The worker name to use
     */
    private String name;
    
    /**
     * The cluster of worker 
     */
    private String cluster;
    
    /**
     * The tenant value for multi-tenant environment
     */
    private String tenant;
    
    /**
     * The level of parallelism
     */
    private Long parallelism;
    
    /**
     * The frequency of polling update (milliseconds)
     */
    private Long pollingRate;
    
    /**
     * The frequency of heartbeat update (milliseconds)
     */
    private Long heartbeatRate;
    
    /**
     * The maximum number of trying to register an agent 
     */
    private Integer workerRegistrationTries;
    
    /**
     * The type of persistence
     */
    private PersistenceType persistenceType;
    
    /**
     * The type of clustering
     */
    private ClusteringType clusteringType;
    
    /**
     * The data source
     */
    private String dataSource;
    
    /**
     * The data source URI
     */
    private String dataSourceUri;
    
    /**
     * The data source username
     */
    private String dataSourceUsername;
    
    /**
     * The data source password
     */
    private String dataSourcePassword;
}
