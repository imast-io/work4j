package io.imast.work4j.worker.job;

import io.imast.core.Zdt;
import java.time.ZoneId;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * A dry-running job implementation for testing purpose
 * 
 * @author davitp
 */
@Slf4j
public class DryRunJob extends BaseQuartzJob {
    
    /**
     * The job implementation based on quartz 
     * 
     * @param arg0 The job execution context
     * @throws JobExecutionException 
     */
    private void executeImpl(JobExecutionContext arg0) throws JobExecutionException {
        
        // get job definition
        var definition = this.getJobDefinition(arg0);
        
        // get code from job 
        var code = definition == null ? "NO_DEFINITION" : definition.getCode();
        
        log.info(String.format("Executed dry-run job %s at %s", code, Zdt.now(ZoneId.systemDefault().toString())));
    }
    
    
    /**
     * The job implementation based on quartz 
     * 
     * @param arg0 The job execution context
     * @throws JobExecutionException 
     */
    @Override
    public void execute(JobExecutionContext arg0) throws JobExecutionException {
        this.executeImpl(arg0);
    }
}