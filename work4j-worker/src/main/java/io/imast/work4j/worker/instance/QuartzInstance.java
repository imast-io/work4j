package io.imast.work4j.worker.instance;

import io.imast.core.Str;
import io.imast.work4j.model.execution.ExecutionStatus;
import io.imast.work4j.model.execution.JobExecution;
import lombok.extern.slf4j.Slf4j;
import io.imast.work4j.worker.WorkerException;
import io.imast.work4j.worker.WorkerFactory;
import java.util.Set;
import java.util.stream.Collectors;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.matchers.GroupMatcher;

/**
 * A quartz worker implementation
 * 
 * @author davitp
 */
@Slf4j
public class QuartzInstance {
    
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
     * @param scheduler The scheduler instance
     * @param factory The controller factory
     */
    public QuartzInstance(Scheduler scheduler, WorkerFactory factory){
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
     * Schedules the job execution
     * 
     * @param execution The job execution to schedule
     * @throws io.imast.work4j.worker.WorkerException
     */
    public void schedule(JobExecution execution) throws WorkerException{
        
        if(execution == null){
            return;
        }
        
        synchronized(this.scheduler){
            this.scheduleImpl(execution);
        }
    }
    
    /**
     * Pause the job execution
     * 
     * @param id The execution id
     * @throws io.imast.work4j.worker.WorkerException
     */
    public void pause(String id) throws WorkerException{
        
        if(Str.blank(id)){
            return;
        }
        
        synchronized(this.scheduler){
            this.pauseImpl(id);
        }
    }
    
    /**
     * Resume the job execution
     * 
     * @param id The execution id
     * @throws io.imast.work4j.worker.WorkerException
     */
    public void resume(String id) throws WorkerException{
        
        if(Str.blank(id)){
            return;
        }
        
        synchronized(this.scheduler){
            this.resumeImpl(id);
        }
    }
        
    /**
     * Unschedule the job
     * 
     * @param id The execution id
     * @param jobId The job id
     * @throws io.imast.work4j.worker.WorkerException
     */
    public void unschedule(String id, String jobId) throws WorkerException{
        
        // nothing to do
        if(Str.blank(id) || Str.blank(jobId)){
            return;
        }
        
        synchronized(this.scheduler){
            this.unscheduleImpl(id, jobId);
        }
    }
    
    /**
     * Gets all group names
     * 
     * @return Returns group names
     * @throws io.imast.work4j.worker.WorkerException
     */
    public Set<String> getExecutions() throws WorkerException{
        try { 
            return this.scheduler.getJobKeys(GroupMatcher.anyJobGroup()).stream().map(key -> key.getName()).collect(Collectors.toSet());
        }
        catch(SchedulerException ex){
            throw new WorkerException("Unable to read job execution keys", ex);
        }
    }
    
    /**
     * Get the paused execution ids
     * 
     * @return Returns the paused executions
     * @throws WorkerException 
     */
    public Set<String> getPausedExecutions() throws WorkerException {
    
        try {
            return this.scheduler.getPausedTriggerGroups();
        }
        catch(SchedulerException ex){
            throw new WorkerException("Unable to read paused job execution keys (trigger groups)", ex);
        }
    }
    
    /**
     * Gets all jobs in the group
     * 
     * @param group The group to select
     * @return Returns jobs in group
     * @throws io.imast.work4j.worker.WorkerException
     */
    public Set<String> getJobs(String group) throws WorkerException {
        try { 
            return this.scheduler.getJobKeys(GroupMatcher.groupEquals(group)).stream().map(k -> k.getName()).collect(Collectors.toSet());
        }
        catch(SchedulerException ex){
            throw new WorkerException("Unable to read job group names", ex);
        }
    }
    
    /**
     * Schedules the job execution
     * 
     * @param execution The job execution to schedule
     */
    protected void scheduleImpl(JobExecution execution) throws WorkerException{
        
        // the job key
        var key = JobKey.jobKey(execution.getId(), execution.getJobId());
        
        try {
            // check if job exists
            var exists = this.scheduler.checkExists(key);
            
            // do not create if exists
            if(exists){
                log.warn("QuartzInstance: Unable to schedule job that has been already scheduled");
                return;
            }
            
            // try create job
            var jobDetail = this.factory.createJob(key, execution);
            
            // unschedule if exists
            if(jobDetail == null){
                log.error("QuartzInstance: Unable to create job via factory");
                return;
            }
            
            // init data
            this.factory.initJob(jobDetail, execution);
            
            // create triggers
            var triggers = this.factory.createTriggers(execution);
            
            // if initial status should be paused, then hold triggers
            if(execution.getStatus() == ExecutionStatus.PAUSED){
                this.pauseImpl(execution.getId());
            }
            
            // add job to scheduler;
            this.scheduler.scheduleJob(jobDetail, triggers, true);
            
            log.info(String.format("QuartzInstance: Job Execution %s%s (ID: %s) is scheduled", execution.getFolder(), execution.getName(), execution.getId()));
        }
        catch(SchedulerException error){
            throw new WorkerException(String.format("QuartzInstance: Failed to schedule the job execution %s", execution.getId()), error);
        }
    }
        
    /**
     * Unschedule the job
     * 
     * @param id The execution id
     * @param jobId The job id
     */
    protected void unscheduleImpl(String id, String jobId) throws WorkerException{
        
        // try the unschedule procedure
        try {
            // remove job
            this.scheduler.deleteJob(JobKey.jobKey(id, jobId));

            log.info(String.format("QuartzInstance: Job Execution %s is unscheduled", id));
        }
        catch (SchedulerException error){
            throw new WorkerException(String.format("QuartzInstance: Failed to unschedule the job %s", id), error);
        }
    }
        
    /**
     * Pauses the job execution with given ID (all its triggers)
     * 
     * @param id The ID of job execution
     * @throws io.imast.work4j.worker.WorkerException
     */
    protected void pauseImpl(String id) throws WorkerException{
        try {
            // pause trigger group identifier by job execution id
            this.scheduler.pauseTriggers(GroupMatcher.triggerGroupEquals(id));
        } catch (SchedulerException error) {
            throw new WorkerException(String.format("QuartzInstance: Failed to pause the triggers of job execution %s", id), error);
        }
    }
    
    /**
     * Pauses the job execution with given ID (all its triggers)
     * 
     * @param id The ID of job execution
     * @throws io.imast.work4j.worker.WorkerException
     */
    protected void resumeImpl(String id) throws WorkerException{
        try {
            // pause trigger group identifier by job execution id
            this.scheduler.resumeTriggers(GroupMatcher.triggerGroupEquals(id));
        } catch (SchedulerException error) {
            throw new WorkerException(String.format("QuartzInstance: Failed to resume the triggers of job execution %s", id), error);
        }
    }
}
