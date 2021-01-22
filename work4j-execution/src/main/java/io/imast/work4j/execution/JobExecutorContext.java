package io.imast.work4j.execution;

import java.util.Map;

/**
 * The API for accessing executor context within scheduler
 * 
 * @author davitp
 */
public interface JobExecutorContext {
    
    /**
     * Get job code
     * 
     * @return Returns job code
     */
    public String getCode();
    
    /**
     * Get job group
     * 
     * @return Returns job group
     */
    public String getGroup();
    
    /**
     * Get job type
     * 
     * @return Returns job type
     */
    public String getType();
    
    /**
     * The a module instance identified by given key
     * 
     * @param <T> The type of module to cast to
     * @param key The key of module within the type of job
     * @param defaultValue The default value to consider if missing
     * @return Returns the registered module instance if any or default
     */
    public <T> T getModuleOr(String key, T defaultValue);
    
    /**
     * Gets the job value by given key
     * 
     * @param <T> The type of job payload value
     * @param key The key of value
     * @param defaultValue The default value to consider
     * @return Returns the value identified by given key 
     */
    public <T> T getJobValueOr(String key, T defaultValue);
    
    /**
     * Gets the trigger value by given key
     * 
     * @param <T> The type of trigger payload value
     * @param key The key of value
     * @param defaultValue The default value to consider
     * @return Returns the value identified by given key 
     */
    public <T> T getTriggerValue(String key, T defaultValue);
    
    /**
     * Gets the (trigger or job) value by given key
     * 
     * @param <T> The type of (trigger or job) payload value
     * @param key The key of value
     * @param defaultValue The default value to consider
     * @return Returns the value identified by given key 
     */
    public <T> T getValue(String key, T defaultValue);
    
    /**
     * Sets the output payload
     * 
     * @param payload The output payload
     */
    public void setOutput(Map<String, Object> payload);
}
