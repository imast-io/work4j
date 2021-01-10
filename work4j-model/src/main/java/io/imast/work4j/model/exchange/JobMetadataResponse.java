package io.imast.work4j.model.exchange;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The metadata exchange response
 * 
 * @author davitp
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobMetadataResponse {
    
    /**
     * The cluster
     */
    private String cluster;
    
    /**
     * The group identities 
     */
    private List<String> groups;
    
    /**
     * The job types
     */
    private List<String> types;
}
