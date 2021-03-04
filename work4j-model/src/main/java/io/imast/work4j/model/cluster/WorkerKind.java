package io.imast.work4j.model.cluster;

import java.io.Serializable;

/**
 * The worker kind within cluster
 * 
 * @author davitp
 */
public enum WorkerKind implements Serializable {
    
    /**
     * The exclusive worker (only one) for this cluster
     */
    EXCLUSIVE,
    
    /**
     * The standalone replica worker kind 
     */
    REPLICA,
    
    /**
     * The balanced worker kind to share load within cluster
     */
    BALANCED
}
