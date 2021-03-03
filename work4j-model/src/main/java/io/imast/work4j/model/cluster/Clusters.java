package io.imast.work4j.model.cluster;

import java.util.regex.Pattern;

/**
 * The clusters constants
 * 
 * @author davitp
 */
public class Clusters {
    
    /**
     * The worker name regex pattern
     */
    public static final Pattern WORKER_REGEX = Pattern.compile("^[a-zA-Z0-9_-]+$");
    
    /**
     * The cluster regex pattern
     */
    public static final Pattern CLUSTER_REGEX = Pattern.compile("^(\\/[A-Za-z0-9_]+)*\\/$");
}
