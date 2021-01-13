package io.imast.work4j.channel;

/**
 * The update operations
 * 
 * @author davitp
 */
public enum UpdateOperation {
    
    /**
     * Add definition to scheduler
     */
    ADD,
    
    /**
     * Update existing definition
     */
    UPDATE,
    
    /**
     * Remove definition from scheduler
     */
    REMOVE
}
