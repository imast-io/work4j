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
     * The cancellation severity in case of manual interrupt
     */
    CANCELLATION,
    
    /**
     * The indication of unsuccessful completion
     */
    FAIL
}
