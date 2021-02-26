package io.imast.work4j.data.exception;

import io.imast.work4j.model.validate.DataValidation;

/**
 * The scheduler data exception 
 * 
 * @author davitp
 */
public class SchedulerDataException extends Exception {
    
    /**
     * The validation model
     */
    private final DataValidation validation;
    
    /**
     * Creates new scheduler data exception
     * 
     * @param message The exception message
     * @param validation The validation info
     * @param cause The exception cause
     */
    public SchedulerDataException(String message, DataValidation validation, Throwable cause){
        super(message, cause);
        this.validation = validation;
    }
    
    /**
     * Creates scheduler data exception
     * 
     * @param message The message of exception
     * @param validation The validation info
     */
    public SchedulerDataException(String message, DataValidation validation){
        this(message, validation, null);
    }
    
    /**
     * Creates scheduler data exception
     * 
     * @param message The message of exception
     */
    public SchedulerDataException(String message){
        this(message, null);
    }
    
    /**
     * Creates scheduler data exception
     * 
     * @param cause The cause of exception
     */
    public SchedulerDataException(Throwable cause){
        super(cause);
        this.validation = null;
    }
    
    /**
     * Gets the validation data
     * 
     * @return Returns the validation info
     */
    public DataValidation getValidation(){
        return this.validation;
    }
}
