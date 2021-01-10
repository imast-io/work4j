package io.imast.work4j.model.agent;

/**
 * The type of agent signals
 * 
 * @author davitp
 */
public enum AgentActivityType {
    
    /**
     * Agent signals registration
     */
    REGISTER,
    
    /**
     *
     * Agent signals heartbeat 
     */
    HEARTBEAT,
    
    /**
     * Agents shuts down
     */
    SHUTDOWN
}
