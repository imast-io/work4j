package io.imast.work4j.model;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The job execution options
 * 
 * @author davitp
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JobExecutionOptions implements Serializable {
    
    /**
     * The option controls reporting iteration results to controller.
     * In case of silent reporting the iteration success/failure will not be reported.
     */
    private boolean silentIterations;   
}
