package io.imast.work4j.model.exchange;

import java.time.ZonedDateTime;
import java.util.HashMap;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The status exchange request
 * 
 * @author davitp
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobStatusExchangeRequest {
    
    /**
     * The cluster
     */
    private String cluster;
    
    /**
     * The last known job state
     */
    private HashMap<String, ZonedDateTime> state;
}
