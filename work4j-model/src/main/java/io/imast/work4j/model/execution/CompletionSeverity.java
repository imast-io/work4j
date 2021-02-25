package io.imast.work4j.model.execution;

import java.io.Serializable;

/**
 * The completion severity definition 
 * 
 * @author davitp
 */
public enum CompletionSeverity implements Serializable {
    
    /**
     * The indication of successful completion
     */
    SUCCESS,
    
    /**
     * The indication of successful completion with warnings
     */
    WARNING,
    
    /**
     * The completion is not complete (for example, due to cancellation)
     */
    INCOMPLETE,
    
    /**
     * The indication of unsuccessful completion
     */
    FAIL
}
