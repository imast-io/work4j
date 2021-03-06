package io.imast.work4j.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The trigger definition structure
 * 
 * @author davitp
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TriggerDefinition implements Serializable {
    
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
    private Long period;
    
    /**
     * The start time for trigger
     */
    private Date startAt;
    
    /**
     * The end time for trigger
     */
    private Date endAt;
    
    /**
     * The trigger data payload
     */
    private Map<String, Object> payload;
    
    /**
     * The job timezone
     */
    private String timezone;
}
