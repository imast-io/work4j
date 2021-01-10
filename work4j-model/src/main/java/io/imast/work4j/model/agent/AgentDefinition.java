package io.imast.work4j.model.agent;

import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The Agent Definition
 * 
 * @author davitp
 */
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class AgentDefinition {
    
    /**
     * The agent ID
     */
    private String id;
    
    /**
     * The supervisor
     */
    private Boolean supervisor;
    
    /**
     * The cluster name
     */
    private String cluster;
    
    /**
     * The worker name
     */
    private String worker;
    
    /**
     * The agent name
     */
    private String name;
    
    /**
     * The agent health
     */
    private AgentHealth health;
    
    /**
     * The declared expectation of health signals
     */
    private Double expectedSignalMinutes;
        
    /**
     * The last signal from agent
     */
    private ZonedDateTime registered;
}
