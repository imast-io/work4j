package io.imast.work4j.model.execution;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * The execution index response
 * 
 * @author davitp
 */
@Data
@AllArgsConstructor
@Builder(toBuilder = true)
public class ExecutionIndexResponse {
        
    /**
     * The set of execution index entries
     */
    private List<ExecutionIndexEntry> entries;
}
