package io.imast.work4j.worker.job;

import io.imast.work4j.model.JobDefinition;
import io.imast.work4j.worker.JobConstants;
import java.util.Map;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

/**
 * The base quartz job 
 * 
 * @author davitp
 */
public abstract class BaseQuartzJob implements Job {
    
    /**
     * Get the job definition
     * 
     * @param executionContext The execution context
     * @return Returns the job definition
     */
    protected JobDefinition getJobDefinition(JobExecutionContext executionContext){
        return JobOps.getJobDefinition(executionContext);
    }
    
    /**
     * Get the job definition
     * 
     * @param executionContext The execution context
     * @return Returns the job definition
     */
    protected Map<String, Object> getData(JobExecutionContext executionContext){
        var def = JobOps.getJobDefinition(executionContext);
        
        if(def != null && def.getJobData() != null && def.getJobData().getData() != null){
            return def.getJobData().getData();
        }
        
        return Map.of();
    }
    
    /**
     * Get type of job definition
     * 
     * @param executionContext The job execution context
     * @return Returns job type
     */
    protected String getType(JobExecutionContext executionContext){
        
        // get definition
        var definition = this.getJobDefinition(executionContext);
        
        return definition == null ? JobConstants.UNKNOWN_JOB_TYPE : definition.getType();
    }
    
    /**
     * Get the context module with the given key
     * 
     * @param <T> The type of module
     * @param key The module key
     * @param executionContext The execution context
     * @return Returns the module from context if given
     */
    protected <T> T getContextModule(String key, JobExecutionContext executionContext){
        return JobOps.getContextModule(key, this.getType(executionContext), executionContext);
    }
}
