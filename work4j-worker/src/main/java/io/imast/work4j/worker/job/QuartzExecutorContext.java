package io.imast.work4j.worker.job;

import io.imast.work4j.execution.JobExecutorContext;
import io.imast.work4j.worker.JobConstants;
import java.util.Map;
import org.quartz.JobExecutionContext;

/**
 * The quartz executor context 
 * 
 * @author davitp
 */
public class QuartzExecutorContext implements JobExecutorContext {

    /**
     * The internal quartz-based job context
     */
    private final JobExecutionContext context;
    
    /**
     * Creates new quartz executor context
     * 
     * @param context The inner-quartz context
     */
    public QuartzExecutorContext(JobExecutionContext context){
        this.context = context;
    }
    
    /**
     * Get job code
     * 
     * @return Returns job code
     */
    @Override
    public String getCode(){
        return JobOps.getValue(this.context.getJobDetail().getJobDataMap(), JobConstants.PAYLOAD_JOB_CODE);
    }
    
    /**
     * Get job group
     * 
     * @return Returns job group
     */
    @Override
    public String getGroup(){        
        return JobOps.getValue(this.context.getJobDetail().getJobDataMap(), JobConstants.PAYLOAD_JOB_FOLDER);
    }
    
    /**
     * Get job type
     * 
     * @return Returns job type
     */
    @Override
    public String getType(){
        return JobOps.getValue(this.context.getJobDetail().getJobDataMap(), JobConstants.PAYLOAD_JOB_TYPE);
    }
    
    /**
     * The a module instance identified by given key
     * 
     * @param <T> The type of module to cast to
     * @param key The key of module within the type of job
     * @param defaultValue The default value to consider if missing
     * @return Returns the registered module instance if any or default
     */
    @Override
    public <T> T getModuleOr(String key, T defaultValue){
        return JobOps.getContextModule(key, this.getType(), this.context);
    }
    
    /**
     * Gets the job value by given key
     * 
     * @param <T> The type of job payload value
     * @param key The key of value
     * @param defaultValue The default value to consider
     * @return Returns the value identified by given key 
     */
    @Override
    public <T> T getJobValueOr(String key, T defaultValue){
        return JobOps.getValueOr(this.context.getJobDetail().getJobDataMap(), key, defaultValue);
    }
    
    /**
     * Gets the trigger value by given key
     * 
     * @param <T> The type of trigger payload value
     * @param key The key of value
     * @param defaultValue The default value to consider
     * @return Returns the value identified by given key 
     */
    @Override
    public <T> T getTriggerValue(String key, T defaultValue){
        return JobOps.getValueOr(this.context.getTrigger().getJobDataMap(), key, defaultValue);
    }
    
    /**
     * Gets the (trigger or job) value by given key
     * 
     * @param <T> The type of (trigger or job) payload value
     * @param key The key of value
     * @param defaultValue The default value to consider
     * @return Returns the value identified by given key 
     */
    @Override
    public <T> T getValue(String key, T defaultValue){
        return JobOps.getValueOr(this.context.getMergedJobDataMap(), key, defaultValue);
    }
    
    /**
     * Sets the output payload
     * 
     * @param payload The output payload
     */
    @Override
    public void setOutput(Map<String, Object> payload){
        this.context.setResult(payload);
    }
}
