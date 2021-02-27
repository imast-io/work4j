package io.imast.work4j.data.exception;

import java.util.ArrayList;
import java.util.List;

/**
 * The scheduler data exception 
 * 
 * @author davitp
 */
public class SchedulerDataException extends RuntimeException {
    
    /**
     * The error messages
     */
    private final List<String> errors;
    
    /**
     * Creates new scheduler data exception
     * 
     * @param message The exception message
     * @param errors The error messages
     * @param cause The exception cause
     */
    public SchedulerDataException(String message, List<String> errors, Throwable cause){
        super(message, cause);
        this.errors = errors == null ? new ArrayList<>() : errors;
    }
    
    /**
     * Creates scheduler data exception
     * 
     * @param message The message of exception
     * @param errors The set of error messages
     */
    public SchedulerDataException(String message, List<String> errors){
        this(message, errors, null);
    }
    
    /**
     * Creates scheduler data exception
     * 
     * @param message The message of exception
     */
    public SchedulerDataException(String message){
        this(message, new ArrayList<>());
    }
    
    /**
     * Creates scheduler data exception
     * 
     * @param cause The cause of exception
     */
    public SchedulerDataException(Throwable cause){
        super(cause);
        this.errors = new ArrayList<>();
    }
    
    /**
     * Gets the error messages
     * 
     * @return Returns the error messages
     */
    public List<String> getErrors(){
        return this.errors;
    }
}
