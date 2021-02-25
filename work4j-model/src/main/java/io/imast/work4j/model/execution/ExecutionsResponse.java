package io.imast.work4j.model.execution;

import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * The response structure of executions
 * 
 * @author davitp
 */
@Data
@Builder(toBuilder = true)
public class ExecutionsResponse {
    
    /**
     * The set of execution identifiers to get
     */
    private List<String> executions;
}
