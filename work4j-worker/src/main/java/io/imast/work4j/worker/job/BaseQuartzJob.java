package io.imast.work4j.worker.job;

import io.imast.work4j.worker.JobConstants;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

/**
 * The base quartz job 
 * 
 * @author davitp
 */
public abstract class BaseQuartzJob implements Job {
    
    /**
     * Get type of job code
     * 
     * @param executionContext The job execution context
     * @return Returns job code
     */
    protected String getCode(JobExecutionContext executionContext){
        return JobOps.getValue(executionContext.getJobDetail().getJobDataMap(), JobConstants.PAYLOAD_JOB_CODE);
    }
    
    /**
     * Get type of job group
     * 
     * @param executionContext The job execution context
     * @return Returns job group
     */
    protected String getGroup(JobExecutionContext executionContext){
        return JobOps.getValue(executionContext.getJobDetail().getJobDataMap(), JobConstants.PAYLOAD_JOB_GROUP);
    }
    
    /**
     * Get value from trigger or job data map
     * 
     * @param <T> The type of value
     * @param executionContext The job execution context
     * @param key The key of trigger data entry
     * @return Returns trigger or job data map value
     */
    protected <T> T getDataValue(JobExecutionContext executionContext, String key){
        return JobOps.getValue(executionContext.getMergedJobDataMap(), key);
    }
    
    /**
     * Get value from trigger or job data map
     * 
     * @param <T> The type of value
     * @param executionContext The job execution context
     * @param key The key of trigger data entry
     * @param defaultValue The default value
     * @return Returns trigger or job data map value
     */
    protected <T> T getDataValue(JobExecutionContext executionContext, String key, T defaultValue){
        return JobOps.getValueOr(executionContext.getMergedJobDataMap(), key, defaultValue);
    }
    
    /**
     * Get value from job data
     * 
     * @param <T> The type of value
     * @param executionContext The job execution context
     * @param key The key of job data entry
     * @return Returns job data value
     */
    protected <T> T getJobDataValue(JobExecutionContext executionContext, String key){
        return JobOps.getValue(executionContext.getJobDetail().getJobDataMap(), key);
    }
    
    /**
     * Get value from trigger data
     * 
     * @param <T> The type of value
     * @param executionContext The job execution context
     * @param key The key of trigger data entry
     * @return Returns trigger data value
     */
    protected <T> T getTriggerDataValue(JobExecutionContext executionContext, String key){
        return JobOps.getValue(executionContext.getTrigger().getJobDataMap(), key);
    }
    
    /**
     * Get type of job definition
     * 
     * @param executionContext The job execution context
     * @return Returns job type
     */
    protected String getType(JobExecutionContext executionContext){
        return JobOps.getValue(executionContext.getJobDetail().getJobDataMap(), JobConstants.PAYLOAD_JOB_TYPE);
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
