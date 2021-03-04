package io.imast.work4j.worker;

/**
 * The type of worker clustering
 * 
 * @author davitp
 */
public enum ClusteringType {
    
    /**
     * The exclusive member of cluster
     */
    EXCLUSIVE,
    
    /**
     * The replica member of cluster
     */
    REPLICA,
    
    /**
     * The balanced member of cluster
     */
    BALANCED
}
