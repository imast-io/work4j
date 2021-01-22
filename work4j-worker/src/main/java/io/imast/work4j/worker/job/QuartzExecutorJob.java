package io.imast.work4j.worker.job;

import io.imast.core.Str;
import io.imast.work4j.execution.JobExecutorException;
import io.vavr.control.Try;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * The base quartz job 
 * 
 * @author davitp
 */
public class QuartzExecutorJob implements Job {

    /**
     * Create and invoke corresponding executor by type 
     * 
     * @param context The execution context
     * @throws JobExecutionException Throws if something went wrong
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        
        // wrap context into an abstract type
        var executorContext = new QuartzExecutorContext(context);
        
        // try get type
        var type = executorContext.getType();
        
        // nothing to do without type
        if(Str.blank(type)){
            throw new JobExecutionException("Type is missing");
        }
        
        // try get supplier
        var supplier = JobOps.getExecutor(type, context);
        
        // could not find supplier, something went wrong
        if(supplier == null){
            throw new JobExecutionException("The requested job type is not supporeted");
        }
        
        // create new instance using supplier
        var tryInstance = Try.of(() -> supplier.apply(executorContext));
        
        // could not create instance from supplier, almost not possible 
        if(!tryInstance.isSuccess()){
            throw new JobExecutionException("Unable to create an executor from given supplier", tryInstance.getCause());
        }
        
        // could not create instance from supplier, almost not possible 
        if(tryInstance.get() == null){
            throw new JobExecutionException("Supplier of executor returned null. Cannot execute...");
        }
        
        // try execute job and report otherwise
        try {
            tryInstance.get().execute();
        } catch (JobExecutorException ex) {
            throw new JobExecutionException("Error while executing the job instance", ex);
        }
    }   
}
