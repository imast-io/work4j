package io.imast.work4j.model;

import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The result of job query
 * 
 * @author davitp
 * @param <T> The type of job definition
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobRequestResult implements Serializable { 
    
    /**
     * The set of result jobs
     */
    private List<JobDefinition> jobs;
    
    /**
     * The total number of jobs
     */
    private Long total;
}
