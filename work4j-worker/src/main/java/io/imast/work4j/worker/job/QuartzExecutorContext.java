package io.imast.work4j.worker.job;

import io.imast.work4j.execution.JobExecutorContext;
import io.imast.work4j.worker.JobConstants;
import java.util.HashMap;
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
     * Get job execution id
     * 
     * @return Returns job execution id
     */
    @Override
    public String getExecutionId(){
        return JobOps.getValue(this.context.getJobDetail().getJobDataMap(), JobConstants.PAYLOAD_JOB_EXECUTION_ID);
    }
    
    /**
     * Get job definition id
     * 
     * @return Returns job definition id
     */
    @Override
    public String getDefinitionId(){
        return JobOps.getValue(this.context.getJobDetail().getJobDataMap(), JobConstants.PAYLOAD_JOB_DEFINITION_ID);
    }
    
    /**
     * Get job code
     * 
     * @return Returns job code
     */
    @Override
    public String getName(){
        return JobOps.getValue(this.context.getJobDetail().getJobDataMap(), JobConstants.PAYLOAD_JOB_NAME);
    }
    
    /**
     * Get job group
     * 
     * @return Returns job group
     */
    @Override
    public String getFolder(){        
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
     * Puts the value into output
     * 
     * @param key The output entry key
     * @param value The output entry value
     */
    @Override
    public void putOutput(String key, Object value){
        
        if(this.context.getResult() == null){
            this.context.setResult(new HashMap<String, Object>());
        }
        
        ((Map<String, Object>)this.context.getResult()).put(key, value);
    }
    
    /**
     * Puts the value into output
     * 
     * @param other The other values
     */
    @Override
    public void putOutput(Map<String, Object> other){
        
        if(this.context.getResult() == null){
            this.context.setResult(new HashMap<String, Object>());
        }
        
        ((Map<String, Object>)this.context.getResult()).putAll(other);
    }
}
