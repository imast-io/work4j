package io.imast.samples.scheduler.worker;

import io.imast.core.discovery.StaticDiscoveryClient;
import java.util.Map;

/**
 * The simple static discovery client
 * 
 * @author davitp
 */
public class SimpleDiscoveryClient extends StaticDiscoveryClient {
    
    /**
     * Creates simple static discovery
     * 
     * @param environment The environment
     */
    public SimpleDiscoveryClient(String environment) {
        super(environment, getPorts());
    }
    
    /**
     * Get static ports
     * 
     * @return Returns static ports
     */
    private static Map<String, Integer> getPorts(){
        return Map.of("scheduler", 8801);
    }
}
