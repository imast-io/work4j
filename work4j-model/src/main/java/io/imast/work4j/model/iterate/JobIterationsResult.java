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
 * @param <T> The type of job iteration
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JobIterationsResult <T extends JobIteration> {
    
    /**
     * The job iterations
     */
    private List<T> results;
    
    /**
     * The total number of items
     */
    private long total;
    
}
