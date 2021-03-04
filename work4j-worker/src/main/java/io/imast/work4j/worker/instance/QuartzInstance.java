package io.imast.work4j.worker.instance;

import io.imast.core.Str;
import io.imast.work4j.model.TriggerDefinition;
import io.imast.work4j.model.execution.ExecutionStatus;
import io.imast.work4j.model.execution.JobExecution;
import io.imast.work4j.worker.JobConstants;
import lombok.extern.slf4j.Slf4j;
import io.imast.work4j.worker.WorkerException;
import io.imast.work4j.worker.job.QuartzExecutorJob;
import io.vavr.control.Try;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;
import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
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
     * Creates new quartz worker instance
     * 
     * @param scheduler The scheduler instance
     */
    public QuartzInstance(Scheduler scheduler){
        this.scheduler = scheduler;
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
     * @param key The execution key
     * @throws io.imast.work4j.worker.WorkerException
     */
    public void pause(ExecutionKey key) throws WorkerException{
        
        synchronized(this.scheduler){
            this.pauseImpl(key);
        }
    }
    
    /**
     * Resume the job execution
     * 
     * @param key The execution key
     * @throws io.imast.work4j.worker.WorkerException
     */
    public void resume(ExecutionKey key) throws WorkerException{
        
        synchronized(this.scheduler){
            this.resumeImpl(key);
        }
    }
        
    /**
     * Unschedule the job
     * 
     * @param key The execution key
     * @throws io.imast.work4j.worker.WorkerException
     */
    public void unschedule(ExecutionKey key) throws WorkerException{        
        synchronized(this.scheduler){
            this.unscheduleImpl(key);
        }
    }
    
    /**
     * Gets all group names
     * 
     * @return Returns group names
     * @throws io.imast.work4j.worker.WorkerException
     */
    public Set<ExecutionKey> getExecutions() throws WorkerException{
        try { 
            return this.scheduler.getJobKeys(GroupMatcher.anyJobGroup())
                    .stream()
                    .map(key -> new ExecutionKey(key.getName(), key.getGroup()))
                    .collect(Collectors.toSet());
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
    public Set<ExecutionKey> getPausedExecutions() throws WorkerException {
    
        try {
            return this.scheduler.getPausedTriggerGroups()
                    .stream()
                    .map(ExecutionKey::from)
                    .collect(Collectors.toSet());
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
     * @throws io.imast.work4j.worker.WorkerException
     */
    protected void scheduleImpl(JobExecution execution) throws WorkerException{
        
        var jobKey = this.jobKey(execution);
        
        // the job key
        var key = JobKey.jobKey(jobKey.getExecutionId(), jobKey.getJobId());
        
        try {
            // check if job exists
            var exists = this.scheduler.checkExists(key);
            
            // do not create if exists
            if(exists){
                log.warn("QuartzInstance: Unable to schedule job that has been already scheduled");
                return;
            }
            
            // try create job
            var jobDetail = this.createJob(key, execution);
            
            // unschedule if exists
            if(jobDetail == null){
                log.error("QuartzInstance: Unable to create job via factory");
                return;
            }
            
            // init data
            this.initJob(jobDetail, execution);
            
            // create triggers
            var triggers = this.createTriggers(execution);
            
            // pause the job will make sure nothing runs before finally inserted
            this.pauseImpl(jobKey);
            
            // add job to scheduler
            this.scheduler.scheduleJob(jobDetail, triggers, true);
            
            // if initial status is active then need to resume whatever was paused
            if(execution.getStatus() == ExecutionStatus.ACTIVE){
                this.resumeImpl(jobKey);
            }
            
            log.info(String.format("QuartzInstance: Job Execution %s%s (ID: %s) is scheduled", execution.getFolder(), execution.getName(), execution.getId()));
        }
        catch(SchedulerException error){
            throw new WorkerException(String.format("QuartzInstance: Failed to schedule the job execution %s", execution.getId()), error);
        }
    }
        
    /**
     * Unschedule the job
     * 
     * @param key The execution key
     * @throws io.imast.work4j.worker.WorkerException
     */
    protected void unscheduleImpl(ExecutionKey key) throws WorkerException{
        
        // try the unschedule procedure
        try {
            // remove job
            this.scheduler.deleteJob(JobKey.jobKey(key.getExecutionId(), key.getJobId()));

            log.info(String.format("QuartzInstance: Job Execution %s is unscheduled", key));
        }
        catch (SchedulerException error){
            throw new WorkerException(String.format("QuartzInstance: Failed to unschedule the job %s", key), error);
        }
    }
        
    /**
     * Pauses the job execution with given key (all its triggers)
     * 
     * @param key The key of job execution
     * @throws io.imast.work4j.worker.WorkerException
     */
    protected void pauseImpl(ExecutionKey key) throws WorkerException{
        try {
            // pause trigger group identifier by job execution id
            this.scheduler.pauseTriggers(GroupMatcher.triggerGroupEquals(key.toString()));
        } catch (SchedulerException error) {
            throw new WorkerException(String.format("QuartzInstance: Failed to pause the triggers of job execution %s", key.toString()), error);
        }
    }
    
    /**
     * Pauses the job execution with given key (all its triggers)
     * 
     * @param key The execution key
     * @throws io.imast.work4j.worker.WorkerException
     */
    protected void resumeImpl(ExecutionKey key) throws WorkerException{
        try {
            // pause trigger group identifier by job execution id
            this.scheduler.resumeTriggers(GroupMatcher.triggerGroupEquals(key.toString()));
        } catch (SchedulerException error) {
            throw new WorkerException(String.format("QuartzInstance: Failed to resume the triggers of job execution %s", key.toString()), error);
        }
    }
    
    /**
     * Creates the job corresponding to the execution
     * 
     * @param key The job key
     * @param execution The job execution
     * @return Returns job details instance
     */
    public JobDetail createJob(JobKey key, JobExecution execution){
        
        // instantiate a job to schedule 
        return JobBuilder.newJob(QuartzExecutorJob.class)
                .withIdentity(key)
                .storeDurably(false)
                .build();
    }
    
    /**
     * Creates the job corresponding to the definition
     * 
     * @param job The job to init
     * @param execution The job definition 
     * @return Returns job details instance
     */
    public JobDetail initJob(JobDetail job, JobExecution execution){

        // the data map
        var systemData = new HashMap<String, Object>();
        
        // populte system data
        systemData.put(JobConstants.PAYLOAD_JOB_EXECUTION_ID, execution.getId());
        systemData.put(JobConstants.PAYLOAD_JOB_DEFINITION_ID, execution.getJobId());
        systemData.put(JobConstants.PAYLOAD_JOB_NAME, execution.getName());
        systemData.put(JobConstants.PAYLOAD_JOB_FOLDER, execution.getFolder());
        systemData.put(JobConstants.PAYLOAD_JOB_TYPE, execution.getType());
        systemData.put(JobConstants.PAYLOAD_JOB_CLUSTER, execution.getCluster());
        systemData.put(JobConstants.PAYLOAD_JOB_OPTIONS, execution.getOptions());
        
        // add all system data
        job.getJobDataMap().putAll(systemData);
        
        // check if there is payload in job definition populate in data
        if(execution.getPayload() != null){
            // add all payload data
            job.getJobDataMap().putAll(execution.getPayload());
        }
        
        return job;
    }

    /**
     * Try get time zone from string
     * 
     * @param timezone The given time zone
     * @return Return timezone or null
     */
    private TimeZone getTimezone(String timezone){
        
        // check if timezone is given
        if(Str.blank(timezone)){
            return null;
        }
        
        // try get zone id
        var zoneid = Try.of(() -> ZoneId.of(timezone)).getOrNull();
        
        // no zone
        if(zoneid == null){
            return null;
        }
        
        return TimeZone.getTimeZone(zoneid);
    }
    
    /**
     * Gets the trigger key for given execution and trigger
     * 
     * @param execution The job execution
     * @param trigger The trigger definition
     * @return Returns a unique key for trigger
     */
    protected ExecutionTriggerKey triggerKey(JobExecution execution, TriggerDefinition trigger){
        
        // gets the job key for execution
        var jobKey = this.jobKey(execution);
        
        // return unique name for trigger
        var triggerName = Str.blank(trigger.getName()) ? Str.random(8) : trigger.getName();
        
        return new ExecutionTriggerKey(triggerName, jobKey.toString());
    }
    
    /**
     * Gets the job execution key for given execution instance
     * 
     * @param execution The job execution
     * @return Returns a unique key for execution
     */
    protected ExecutionKey jobKey(JobExecution execution){
        return new ExecutionKey(execution.getId(), execution.getJobId());
    }
    
    /**
     * Creates the set of Cron triggers for the given job
     * 
     * @param execution The job execution
     * @param trigger The trigger definition
     * @return Returns job triggers
     */
    private Set<Trigger> cronTrigger(JobExecution execution, TriggerDefinition trigger){

        // the key
        var key = this.triggerKey(execution, trigger);
        
        // result quartz triggers
        var result = new HashSet<Trigger>();
        
        // cron expression
        var cronExpression = trigger.getCron();
        
        // check validity 
        if(Str.blank(cronExpression) || !CronExpression.isValidExpression(cronExpression)){
            log.warn("WorkerFactory: Skipping cron trigger as it is not valid: " + cronExpression);
            return result;
        }
        
        // try get zone
        var zone = this.getTimezone(trigger.getTimezone());
        
        // build cron schedule
        var schedule = CronScheduleBuilder.cronSchedule(cronExpression).inTimeZone(zone);
        
        // create trigger
        var triggerBuilder = TriggerBuilder.newTrigger()
            .withIdentity(key.getTriggerName(), key.getExecutionKey())
            .withSchedule(schedule);

        // if start time is given
        if(trigger.getStartAt() != null){
            triggerBuilder.startAt(trigger.getStartAt());
        }
        
        // if end time is given
        if(trigger.getEndAt() != null){
            triggerBuilder.endAt(trigger.getEndAt());
        }

        // schedule job with cron trigger
        result.add(triggerBuilder.build());
        
        return result;
    }
    
    /**
     * Creates the set of Cron triggers for the given job
     * 
     * @param execution The job execution
     * @param trigger The trigger definition
     * @return Returns job triggers
     */
    private Set<Trigger> periodTrigger(JobExecution execution, TriggerDefinition trigger){
        // job triggers
        var result = new HashSet<Trigger>();
     
        // period in milliseconds
        var periodMs = trigger.getPeriod();
        
        // if not given
        if(periodMs == null || periodMs == 0.0){
            log.warn("WorkerFactory: Cannot create static period trigger because of missing period.");
            return result;
        }
        
        // convert to seconds
        var periodSecond = (int) (periodMs / 1000.0);
        
        // the key
        var key = this.triggerKey(execution, trigger);
        
        // create trigger
        var triggerBuilder = TriggerBuilder.newTrigger()
                .withIdentity(key.getTriggerName(), key.getExecutionKey())
                .withSchedule(SimpleScheduleBuilder.repeatSecondlyForever(periodSecond));
        
        // if start time is given
        if(trigger.getStartAt() != null){
            triggerBuilder.startAt(trigger.getStartAt());
        }
        
        // if end time is given
        if(trigger.getEndAt() != null){
            triggerBuilder.endAt(trigger.getEndAt());
        }
        
        // add trigger
        result.add(triggerBuilder.build());
        
        return result;
    }
    
    /**
     * Creates the set of Cron triggers for the given job
     * 
     * @param execution The job execution
     * @param trigger The trigger definition
     * @return Returns job triggers
     */
    public Set<Trigger> createOneTimeTriggers(JobExecution execution, TriggerDefinition trigger){
        // job triggers
        var result = new HashSet<Trigger>();
      
        // the key
        var key = this.triggerKey(execution, trigger);
        
        // create trigger
        var triggerBuilder = TriggerBuilder.newTrigger()
                .withIdentity(key.getTriggerName(), key.getExecutionKey());
        
         // if start time is given
        if(trigger.getStartAt() != null){
            triggerBuilder.startAt(trigger.getStartAt());
        } else {
            triggerBuilder.startNow();
        }
        
        // add trigger
        result.add(triggerBuilder.build());
        
        return result;
    }
    
    /**
     * Creates the set of triggers for the given job
     * 
     * @param execution The job execution
     * @return Returns job triggers
     */
    public Set<Trigger> createTriggers(JobExecution execution){
        
        // triggers to traverse
        var triggers = execution.getTriggers();
        
        // nothing to process
        if(triggers == null){
            return Set.of();
        }
        
        // set of final triggers
        var result = new HashSet<Trigger>();
        
        // convert triggers 
        triggers.forEach(trigger -> {
            
            // the trigger payload
            Map<String, Object> triggerPayload = trigger.getPayload() == null ? Map.of() : trigger.getPayload();
            
            // quartz triggers
            Set<Trigger> quartzTriggers = Set.of();
            
            // process based on type
            switch(trigger.getType()){
                case CRON:
                    quartzTriggers = this.cronTrigger(execution, trigger);
                    break;
                case PERIODIC:
                    quartzTriggers = this.periodTrigger(execution, trigger);
                    break;
                case ONCE:
                    quartzTriggers = this.createOneTimeTriggers(execution, trigger);
                    break;
            }
            
            // attach data
            quartzTriggers.forEach(quartzTrigger -> {
                quartzTrigger.getJobDataMap().putAll(triggerPayload);
            });
            
            // add quartz triggers
            result.addAll(quartzTriggers);
        });
        
        return result;
    }
}
