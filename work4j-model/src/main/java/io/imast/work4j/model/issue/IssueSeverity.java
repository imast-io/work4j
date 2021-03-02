package io.imast.work4j.model.issue;

import java.io.Serializable;

/**
 * The issue severity type
 * 
 * @author davitp
 */
public enum IssueSeverity implements Serializable {
    
    /**
     * The debug severity
     */
    DEBUG,
    
    /**
     * The information severity
     */
    INFO,
    
    /**
     * The warning severity
     */
    WARNING,
    
    /**
     * The error severity
     */
    ERROR,
    
    /**
     * The fatal severity
     */
    FATAL
}
