package io.imast.work4j.channel.worker;

import io.imast.work4j.model.execution.JobExecution;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * The message for worker that indicates creation of new execution
 * 
 * @author davitp
 */
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class WorkerExecutionCreated implements WorkerMessage {
    
    /**
     * The execution instance
     */
    private JobExecution execution;    
}
