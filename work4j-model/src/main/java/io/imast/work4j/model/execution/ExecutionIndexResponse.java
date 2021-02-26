package io.imast.work4j.model.execution;

import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * The execution index response
 * 
 * @author davitp
 */
@Data
@Builder(toBuilder = true)
public class ExecutionIndexResponse {
    
    /**
     * The target cluster
     */
    private String cluster;
    
    /**
     * The target tenant
     */
    private String tenant;    
    
    /**
     * The set of execution index entries
     */
    private List<ExecutionIndexEntry> entries;
}
