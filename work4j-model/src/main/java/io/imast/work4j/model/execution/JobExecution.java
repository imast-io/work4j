package io.imast.work4j.model.execution;

import io.imast.work4j.model.JobOptions;
import io.imast.work4j.model.TriggerDefinition;
import java.util.Date;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The job execution instance to run
 * 
 * @author davitp
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class JobExecution {
    
    /**
     * The execution identifier
     */
    private String id;
    
    /**
     * The job definition identifier
     */
    private String jobId;
    
    /**
     * The job definition name
     */
    private String name;
    
    /**
     * The job definition folder
     */
    private String folder;
    
    /**
     * The job definition type
     */
    private String type;
    
    /**
     * The status of execution
     */
    private ExecutionStatus status;
    
    /**
     * The completion severity
     */
    private CompletionSeverity completionSeverity;
    
    /**
     * The set of trigger definitions
     */
    private List<TriggerDefinition> triggers;
       
    /**
     * The job definition tenant
     */
    private String tenant;
    
    /**
     * The cluster for the job
     */
    private String cluster;
    
    /**
     * The execution options
     */
    private JobOptions options;
        
    /**
     * The job data payload
     */
    private Map<String, Object> payload;
    
    /**
     * The user that defined the job
     */
    private String createdBy;
    
    /**
     * The user that modified the job
     */
    private String modifiedBy;
    
    /**
     * Timestamp of creation
     */
    private Date defined;
    
    /**
     * The time of last modification
     */
    private Date modified;
    
    /**
     * The execution instance was created
     */
    private Date submited;
    
    /**
     * The extra information required for execution
     */
    private Map<String, Object> extra;
}
