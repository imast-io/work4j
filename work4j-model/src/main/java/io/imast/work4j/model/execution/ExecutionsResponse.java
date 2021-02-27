package io.imast.work4j.model.execution;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * The response structure of executions
 * 
 * @author davitp
 */
@Data
@AllArgsConstructor
@Builder(toBuilder = true)
public class ExecutionsResponse {
    
    /**
     * The set of execution identifiers to get
     */
    private List<JobExecution> executions;
    
    /**
     * The total number of entries for query
     */
    private long total;
}
