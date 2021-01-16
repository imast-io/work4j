package io.imast.work4j.worker.job;

import io.imast.core.Lang;
import io.imast.core.Str;
import io.imast.work4j.model.JobDefinition;
import io.imast.work4j.worker.JobConstants;
import io.vavr.control.Try;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

/**
 * The job operations 
 * 
 * @author davitp
 */
@Slf4j
public class JobOps {
    
    /**
     * Get the job definition
     * 
     * @param executionContext The execution context
     * @return Returns the job definition
     */
    public static JobDefinition getJobDefinition(JobExecutionContext executionContext){
       
        // the data by key
        var def = Try.of(() -> executionContext.getJobDetail().getJobDataMap().get(JobConstants.JOB_DEFINITION)).getOrElse(() -> null);
        
        // check if data is not valid
        if(def == null || !(def instanceof JobDefinition)){
            log.error("JobOps: Unable to get the job definition.");
            return null;
        }
        
        return (JobDefinition) def;
    }
    
    /**
     * Get the payload data value
     * 
     * @param <T> The type of value
     * @param data The data object to get from
     * @param key The key of data entry
     * @return Returns the job entry value
     */
    public static <T> T getValue(JobDataMap data, String key){
       
        // the value by key
        var value = data.getOrDefault(key, null);
        
        // get value
        if(value == null){
            return null;
        }
        
        // the final value
        return Lang.<T>safeCast(value);
    }
    
    /**
     * Get the payload data value
     * 
     * @param <T> The type of value
     * @param data The data object to get from
     * @param key The key of data entry
     * @param defaultValue The default value
     * @return Returns the job entry value
     */
    public static <T> T getValueOr(JobDataMap data, String key, T defaultValue){
       
        // the value by key
        var value = data.getOrDefault(key, defaultValue);
        
        // get value
        if(value == null){
            return null;
        }
        
        // the final value
        return Lang.<T>safeCast(value);
    }
    
    /**
     * Get the context module with the given key
     * 
     * @param <T> The type of module
     * @param key The module key
     * @param type The job type
     * @param executionContext The execution context
     * @return Returns the module from context if given
     */
    public static <T> T getContextModule(String key, String type, JobExecutionContext executionContext){
        
        // check key
        if(Str.blank(key)){
            log.error("JobOps: Invalid key.");
            return null;
        }
        
        // the scheduler context
        var context = Try.of(() -> executionContext.getScheduler().getContext()).getOrElse(() -> null);
        
        // check if context is not valid
        if(context == null){
            log.error("JobOps: No valid scheduler context.");
            return null;
        }
        
        // the job modules registry
        var jobModules = (Map<String, Map<String, Object>>) context.get(JobConstants.JOB_MODULES);
        
        // check if key is given
        if(!jobModules.containsKey(type)){
            log.error("JobOps: No modules detected for the job type: " + type);
            return null;
        }
        
        // the modules for the given job type
        var modules = jobModules.get(type);
        
        // check if key is given
        if(!modules.containsKey(key)){
            log.error("JobOps: Could not find any module with the given key: " + key);
            return null;
        }
        
        // safely cast the object 
        T module = Lang.safeCast(modules.get(key));
        
        // check if key is given
        if(module == null){
            log.error("JobOps: Could not get module with required type");
            return null;
        }
        
        return module;
    }
    
    /**
     * Build a identity for the job definition 
     * 
     * @param definition The job definition
     * @return Returns an identity key for job definition
     */
    public static String identity(JobDefinition definition){
        return String.format("%s:%s", definition.getCode(), definition.getGroup());
    }
}
