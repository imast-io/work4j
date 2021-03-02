package io.imast.work4j.model.issue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * The data issue entity
 * 
 * @author davitp
 */
@Data
@Builder
@AllArgsConstructor
public class DataIssue {
    
    /**
     * The message of issue
     */
    private String message;
    
    /**
     * The severity of issue
     */
    private IssueSeverity severity;
    
    /**
     * The group of issue
     */
    private String group;
}
