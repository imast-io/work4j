package io.imast.work4j.execution;

/**
 * A special interface to define execution logic of a job
 * 
 * @author davitp
 */
public interface JobExecutor {
    
    /**
     * Executes the specified logic for the single triggered job instance
     * 
     * @throws JobExecutorException In case of any internal errors
     */
    public void execute() throws JobExecutorException;
}
