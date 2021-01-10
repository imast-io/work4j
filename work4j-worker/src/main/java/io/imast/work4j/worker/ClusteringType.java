package io.imast.work4j.worker;

/**
 * The type of agent clustering
 * 
 * @author davitp
 */
public enum ClusteringType {
    
    /**
     * Non-clustered single node agent
     */
    STANDALONE,
    
    /**
     * The JDBC-backed database clustering
     */
    JDBC
}
