package io.imast.work4j.model.worker;

import lombok.Builder;
import lombok.Data;

/**
 * The worker session information
 * 
 * @author davitp
 */
@Data
@Builder(toBuilder = true)
public class WorkerSessionHeartbeat {
    
    /**
     * The last activity in the session
     */
    private WorkerActivity activity;
}
