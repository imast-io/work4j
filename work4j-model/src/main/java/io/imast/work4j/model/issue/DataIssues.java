package io.imast.work4j.model.issue;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The data issues 
 * 
 * @author davitp
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class DataIssues {
    
    /**
     * The set of data issues
     */
    private List<DataIssue> issues;
}
