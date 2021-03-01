package io.imast.work4j.worker;

import io.imast.core.Str;
import io.imast.work4j.execution.JobExecutor;
import io.imast.work4j.execution.JobExecutorContext;
import io.imast.work4j.model.TriggerDefinition;
import io.imast.work4j.model.execution.JobExecution;
import io.imast.work4j.worker.job.QuartzExecutorJob;
import io.vavr.control.Try;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

/**
 * The job factory for job manager
 * 
 * @author davitp
 */
@Slf4j
public class WorkerFactory {
    
    /**
     * The map of job classes 
     */
    protected final Map<String, Function<JobExecutorContext, JobExecutor>> jobClasses;
    
    /**
     * Creates new instance of job factory
     */
    public WorkerFactory(){
        this.jobClasses = new HashMap<>();
    }
    
    /**
     * Get registered types
     * 
     * @return Returns set of types
     */
    public Set<String> getTypes(){
        return this.jobClasses.keySet();
    }
    
    /**
     * Register a class mapping for the given job type
     * 
     * @param type The job type
     * @param executorSupplier The executor supplier
     */
    public void registerExecutor(String type, Function<JobExecutorContext, JobExecutor> executorSupplier){
        this.jobClasses.put(type, executorSupplier);
    }
    
    /**
     * Gets the executor for the given job type
     * 
     * @param type The job type
     * @return Returns executor supplier or null
     */
    public Function<JobExecutorContext, JobExecutor> getExecutor(String type){
        return this.jobClasses.getOrDefault(type, null);
    }
}
