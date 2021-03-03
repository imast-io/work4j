package io.imast.work4j.model.cluster;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The worker heartbeat info
 * 
 * @author davitp
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class WorkerHeartbeat {
    
    /**
     * The last activity in the session
     */
    private WorkerActivity activity;
}
