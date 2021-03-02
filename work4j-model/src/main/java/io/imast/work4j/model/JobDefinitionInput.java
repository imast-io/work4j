package io.imast.work4j.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The Job Definition Input Model
 * 
 * @author davitp
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class JobDefinitionInput implements Serializable {

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
     * The creator user
     */
    private String createdBy;
    
    /**
     * The modified user
     */
    private String modifiedBy;
    
    /**
     * The set of selectors
     */
    private Map<String, String> selectors;
    
    /**
     * The job data payload
     */
    private Map<String, Object> payload;
        
    /**
     * The extra information required for execution
     */
    private Map<String, Object> extra;
}
