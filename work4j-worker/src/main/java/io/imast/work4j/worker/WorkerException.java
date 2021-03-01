package io.imast.work4j.worker;

/**
 * The worker exception type
 * 
 * @author davitp
 */
public class WorkerException extends RuntimeException {
    
    /**
     * Creates new instance of exception
     * 
     * @param msg The message
     * @param inner The inner exception
     */
    public WorkerException(String msg, Throwable inner){
        super(msg, inner);
    }
    
    /**
     * Creates new instance of exception
     * 
     * @param msg The message
     */
    public WorkerException(String msg){
        super(msg, null);
    }
}
