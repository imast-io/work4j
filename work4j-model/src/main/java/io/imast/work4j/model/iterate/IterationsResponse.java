package io.imast.work4j.model.iterate;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The job iterations page
 * 
 * @author davitp
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IterationsResponse {
    
    /**
     * The job iterations
     */
    private List<Iteration> results;
    
    /**
     * The total number of items
     */
    private long total;
    
}
