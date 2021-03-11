package io.imast.samples.scheduler.worker;

import io.imast.work4j.execution.JobExecutorBase;
import io.imast.work4j.execution.JobExecutorContext;
import io.imast.work4j.execution.JobExecutorException;

/**
 * A job that will echo basic message 
 * 
 * @author davitp
 */
public class EchoJob extends JobExecutorBase {
    
    /**
     * Creates new instance based on context
     * 
     * @param context The context of executor
     */
    public EchoJob(JobExecutorContext context){
        super(context);
    }
    
    /**
     * The job implementation  
     * 
     * @throws JobExecutorException 
     */
    @Override
    public void execute() throws JobExecutorException {
            
        // get printer module for this job type
        var printer = this.context.<PrinterModule>getModuleOr("PRINTER", null);
        
        // check if printer module is there
        if(printer == null){
            throw new JobExecutorException("A printer module is required for this job");
        }
        
        // the full path to job
        var path = String.format("%s%s", this.context.getFolder(), this.context.getName());

        // get the message from payload
        var message = this.context.getValue("MESSAGE", "EMPTY MESSAGE");

        // print the given message
        printer.print(String.format("The job %s says %s", path, message));
    }
}
