package io.imast.work4j.model.execution;

import java.util.Map;
import lombok.Builder;
import lombok.Data;

/**
 * The job execution input
 * 
 * @author davitp
 */
@Data
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
