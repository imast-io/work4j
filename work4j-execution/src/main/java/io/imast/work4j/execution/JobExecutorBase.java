package io.imast.work4j.execution;

/**
 * An abstract basic implementation of Job executor interface
 * 
 * @author davitp
 */
public abstract class JobExecutorBase implements JobExecutor {

    /**
     * The execution context
     */
    protected final JobExecutorContext context;
    
    /**
     * Creates new instance based on context
     * 
     * @param context The context of executor
     */
    protected JobExecutorBase(JobExecutorContext context){
        this.context = context;
    }
}
