package io.imast.work4j.execution;

/**
 * The job execution exception
 * 
 * @author davitp
 */
public class JobExecutorException extends Exception {
    
    /**
     * Creates new instance of job execution exception
     * 
     * @param message The message of failure
     */
    public JobExecutorException(String message){
        super(message);
    }
    
    /**
     * Creates new instance of job execution exception
     * 
     * @param message The message of failure
     * @param cause The cause of error
     */
    public JobExecutorException(String message, Throwable cause){
        super(message, cause);
    }
}
