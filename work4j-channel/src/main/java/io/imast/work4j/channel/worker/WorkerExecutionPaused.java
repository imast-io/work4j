package io.imast.work4j.channel.worker;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * The message for worker that indicates that execution is paused
 * 
 * @author davitp
 */
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class WorkerExecutionPaused implements WorkerMessage {
    
    /**
     * The execution id
     */
    private String executionId;  
    
    /**
     * The job id
     */
    private String jobId; 
}
