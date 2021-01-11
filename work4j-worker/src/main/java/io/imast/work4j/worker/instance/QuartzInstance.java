package io.imast.work4j.worker.instance;

import io.imast.core.Str;
import lombok.extern.slf4j.Slf4j;
import io.imast.work4j.model.JobDefinition;
import io.imast.work4j.model.exchange.JobStatusExchangeRequest;
import io.imast.work4j.worker.JobConstants;
import io.imast.work4j.worker.WorkerException;
import io.imast.work4j.worker.WorkerFactory;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import lombok.Getter;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.matchers.GroupMatcher;

/**
 * A quartz worker implementation
 * 
 * @author davitp
 */
@Slf4j
public class QuartzInstance {
    
    /**
     * The worker name
     */
    @Getter
    private final String worker;
    
    /**
     * The cluster name
     */
    @Getter
    private final String cluster;
    
    /**
     * The quartz scheduler instance
     */
    protected final Scheduler scheduler;
    
    /**
     * A factory instance
     */
    protected final WorkerFactory factory;
    
    /**
     * Creates new quartz worker instance
     * 
     * @param worker The worker name
     * @param cluster The cluster name
     * @param scheduler The scheduler instance
     * @param factory The controller factory
     */
    public QuartzInstance(String worker, String cluster, Scheduler scheduler, WorkerFactory factory){
        this.worker = worker;
        this.cluster = cluster;
        this.scheduler = scheduler;
        this.factory = factory;
    }
    
    /**
     * Starts the scheduler
     * 
     * @throws WorkerException 
     */
    public void start() throws WorkerException{
        try {
            this.scheduler.start();
        } catch (SchedulerException ex) {
            throw new WorkerException("Could not start quartz worker", ex);
        }
    }
    
    /**
     * Stops the scheduler
     * 
     * @throws WorkerException 
     */
    public void stop() throws WorkerException{
        try {
            this.scheduler.shutdown();
        } catch (SchedulerException ex) {
            throw new WorkerException("Could not stop quartz worker", ex);
        }
    }
    
    /**
     * Schedules the job definition
     * 
     * @param jobDefinition The job definition to schedule
     */
    public void schedule(JobDefinition jobDefinition){
        
        if(jobDefinition == null){
            return;
        }
        
        synchronized(this.scheduler){
            this.scheduleImpl(jobDefinition);
        }
    }
    
    /**
     * Schedules the job definition
     * 
     * @param jobDefinition The job definition to schedule
     */
    public void reschedule(JobDefinition jobDefinition){
        
        if(jobDefinition == null){
            return;
        }
        
        synchronized(this.scheduler){
            this.rescheduleImpl(jobDefinition);
        }
    }
    
    /**
     * Unschedule the job
     * 
     * @param code The job code
     * @param group The group of job
     */
    public void unschedule(String code, String group){
        
        // nothing to do
        if(Str.blank(code) || Str.blank(group)){
            return;
        }
        
        synchronized(this.scheduler){
            this.unscheduleImpl(code, group);
        }
    }
    
    /**
     * Compute current status for exchange
     * 
     * @param group The job group
     * @param type The job type
     * @return The current status
     */
    public JobStatusExchangeRequest getStatus(String group, String type) {
        synchronized(this.scheduler){
            return this.getStatusImpl(group, type);
        }
    }
    /**
     * Schedules the job definition
     * 
     * @param jobDefinition The job definition to schedule
     */
    protected void scheduleImpl(JobDefinition jobDefinition){
        
        // the job key
        var key = JobKey.jobKey(jobDefinition.getCode(), jobDefinition.getGroup());
        
        try {
            // check if job exists
            var exists = this.scheduler.checkExists(key);
            
            // do not create if exists
            if(exists){
                log.error("QuartzInstance: Unable to schedule job that has been already scheduled");
                return;
            }
            
            // try create job
            var jobDetail = this.factory.createJob(key, jobDefinition);
            
            // unschedule if exists
            if(jobDetail == null){
                log.error("QuartzInstance: Unable to create job via factory");
                return;
            }
            
            // create triggers
            var triggers = this.factory.createTriggers(jobDefinition);
            
            // add job to scheduler;
            this.scheduler.scheduleJob(jobDetail, triggers, true);
        }
        catch(SchedulerException error){
            log.error("QuartzInstance: Failed to schedule the job", error);
        }
    }
    
    /**
     * Schedules the job definition
     * 
     * @param jobDefinition The job definition to schedule
     */
    protected void rescheduleImpl(JobDefinition jobDefinition){
        
        // the job key
        var key = JobKey.jobKey(jobDefinition.getCode(), jobDefinition.getGroup());
        
        try {
            // check if job exists
            boolean exists = this.scheduler.checkExists(key);
            
            // unschedule if exists
            if(!exists){
                log.error("QuartzInstance: Job cannot be updated because it does not exist");
                return;
            }
            
            // try create job
            var jobDetail = this.scheduler.getJobDetail(key);
            
            // unschedule if exists
            if(jobDetail == null){
                log.error("QuartzInstance: Unable to find job by the key factory");
                return;
            }
            
            // update definition to data map
            jobDetail.getJobDataMap().put(JobConstants.JOB_DEFINITION, jobDefinition);
            
            // unschedule the triggers
            this.unscheduleTriggers(key);
            
            // create tricodeggers
            var triggers = this.factory.createTriggers(jobDefinition);
            
            // add job to scheduler;
            this.scheduler.scheduleJob(jobDetail, triggers, true);
        }
        catch(SchedulerException error){
            log.error("QuartzInstance: Failed to schedule the job", error);
        }
    }
    
    /**
     * Unschedule the job
     * 
     * @param jobCode The job code
     * @param jobGroup The group of job
     */
    protected void unscheduleImpl(String jobCode, String jobGroup){
        
        // try the unschedule procedure
        try {
            // the job key
            var key = JobKey.jobKey(jobCode, jobGroup);
            
            // check if job exists
            var exists = this.scheduler.checkExists(key);
            
            // unschedule if exists
            if(!exists){
                log.warn("QuartzInstance: Job cannot be unscheduled because it does not exist");
                return;
            }
            
            // unschedule triggers
            this.unscheduleTriggers(key);
            
            // remove job
            this.scheduler.deleteJob(key);
        }
        catch (SchedulerException error){
            log.error("QuartzInstance: Failed to unschedule the job", error);
        }
    }
    
    /**
     * Unschedule triggers for the given job
     * 
     * @param key The job key
     */
    protected void unscheduleTriggers(JobKey key) {
        
        try{
            // unschedule if exists
            if(!this.scheduler.checkExists(key)){
                return;
            }

            // get triggers of job
            var triggers = this.scheduler.getTriggersOfJob(key);

            // use empty stream
            triggers = triggers == null ? new ArrayList() : triggers;

            // unschedule triggers
            for(Trigger trigger : triggers){
                this.scheduler.unscheduleJob(trigger.getKey());
            }
        }
        catch(SchedulerException error){
            log.error("QuartzInstance: Failed to unschedule the triggers", error);
        }       
    }
    
    /**
     * Compute current status for exchange
     * 
     * @param group The job group
     * @param type The job type
     * @return The current status
     */
    protected JobStatusExchangeRequest getStatusImpl(String group, String type) {
        // new set for status
        var status = new HashMap<String, ZonedDateTime>();
        
        try {
            // get job keys in the group
            var keys = this.scheduler.getJobKeys(GroupMatcher.jobGroupEquals(group));
        
            // process jobs
            for(JobKey jobKey : keys){
                // get the job
                var job = this.scheduler.getJobDetail(jobKey);
                
                // skip if not available
                if(job == null || !job.getJobDataMap().containsKey(JobConstants.JOB_DEFINITION)){
                    continue;
                }
                
                // the job definition
                var jobDefinition = (JobDefinition) job.getJobDataMap().get(JobConstants.JOB_DEFINITION);
                
                // skip jobs of other types
                if(!Str.eq(jobDefinition.getType(), type)){
                    continue;
                }
                
                // record last modified time
                status.put(jobDefinition.getCode(), jobDefinition.getModified());
            }
        }
        catch(SchedulerException error){
            log.error("QuartzInstance: Could not compute status of executing jobs.", error);
        }
                
        return new JobStatusExchangeRequest(group, type, this.cluster, status);
    }
}