package io.imast.work4j.model;

import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The trigger definition instance
 * 
 * @author davitp
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TriggerDefinition {
    
    /**
     * The trigger name
     */
    private String name;
    
    /**
     * The trigger type
     */
    private TriggerType type;
    
    /**
     * The Cron expression
     */
    private String cron;
    
    /**
     * The static period to use in milliseconds
     */
    private Double period;
    
    /**
     * The start time for trigger
     */
    private ZonedDateTime startAt;
    
    /**
     * The end time for trigger
     */
    private ZonedDateTime endAt;
    
    /**
     * The job timezone
     */
    private String timezone;
}