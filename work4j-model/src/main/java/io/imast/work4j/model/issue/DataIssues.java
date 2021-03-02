package io.imast.work4j.model.issue;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * The data issues 
 * 
 * @author davitp
 */
@Data
@AllArgsConstructor
@Builder(toBuilder = true)
public class DataIssues {
    
    /**
     * The set of data issues
     */
    private List<DataIssue> issues;
}
