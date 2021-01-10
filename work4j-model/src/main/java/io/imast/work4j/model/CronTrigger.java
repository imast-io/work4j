package io.imast.work4j.model;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The Cron trigger for the job 
 * 
 * @author davitp
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CronTrigger implements Serializable {
    
    /**
     * The trigger id
     */
    private String id;
    
    /**
     * The Cron expression
     */
    private String expression;
}
