package io.imast.work4j.model.agent;

import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The agent health
 * 
 * @author davitp
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentHealth {
    
    /**
     * The last update timestamp
     */
    private ZonedDateTime lastUpdated; 
    
    /**
     * The agent last activity type
     */
    private AgentActivityType lastActivity;
}
