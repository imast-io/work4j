package io.imast.work4j.channel;

import io.imast.work4j.model.JobDefinition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The update message 
 * 
 * @author davitp
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkerUpdateMessage {
    
    /**
     * The operation to perform
     */
    private UpdateOperation operation;
    
    /**
     * The code of job
     */
    private String code;
    
    /**
     * The job group
     */
    private String group;
    
    /**
     * The definition itself in case of insert/update
     */
    private JobDefinition definition;
}
