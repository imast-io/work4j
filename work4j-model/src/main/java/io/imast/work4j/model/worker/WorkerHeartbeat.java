package io.imast.work4j.model.worker;

import lombok.Builder;
import lombok.Data;

/**
 * The worker heartbeat info
 * 
 * @author davitp
 */
@Data
@Builder(toBuilder = true)
public class WorkerHeartbeat {
    
    /**
     * The last activity in the session
     */
    private WorkerActivity activity;
}
