package io.imast.work4j.model.exchange;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The job metadata exchange request
 * 
 * @author davitp
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobMetadataRequest {
    
    /**
     * The cluster
     */
    private String cluster;
}
