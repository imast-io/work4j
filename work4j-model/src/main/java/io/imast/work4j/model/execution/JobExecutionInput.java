package io.imast.work4j.model.execution;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The job execution input
 * 
 * @author davitp
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class JobExecutionInput {
    
    /**
     * The job definition identifier
     */
    private String jobId;
    
    /**
     * The status of execution
     */
    private ExecutionStatus initialStatus;
    
    /**
     * The cluster for the job
     */
    private String cluster;
        
    /**
     * The job data payload
     */
    private Map<String, Object> payloadOverride;
}
