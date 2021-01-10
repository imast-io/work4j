package io.imast.work4j.worker;

import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * The job manager configuration
 * 
 * @author davitp
 */
@Data
@Builder
public class QuartzWorkerConfiguration {
    
    /**
     * Should supervise job management
     */
    private boolean supervise;
    
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
     * The frequency of job sync
     */
    private Duration jobSyncRate;
    
    /**
     * The frequency of agent updates
     */
    private Duration workerSignalRate;
    
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
