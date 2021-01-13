package io.imast.work4j.worker;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The job manager configuration
 * 
 * @author davitp
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkerConfiguration {
    
    /**
     * The agent name job manager runs on
     */
    private String worker;
    
    /**
     * The manager cluster name
     */
    private String cluster;
    
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
