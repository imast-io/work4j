package io.imast.work4j.model.validate;

import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * The data validation model 
 * 
 * @author davitp
 */
@Data
@Builder(toBuilder = true)
public class DataValidation {
    
    /**
     * The validation messages
     */
    private List<String> messages;
}
