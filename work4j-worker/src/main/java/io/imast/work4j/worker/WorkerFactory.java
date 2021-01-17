package io.imast.work4j.worker;

import io.imast.work4j.worker.job.JobOps;
import io.imast.core.Str;
import io.imast.core.Zdt;
import io.imast.work4j.model.JobDefinition;
import io.imast.work4j.model.TriggerDefinition;
import io.vavr.control.Try;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
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
    protected final Map<String, Class> jobClasses;
    
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
     * @param clazz The class type
     */
    public void registerJobClass(String type, Class clazz){
        this.jobClasses.put(type, clazz);
    }
    
    /**
     * Creates the job corresponding to the definition
     * 
     * @param key The job key
     * @param jobDefinition The job definition
     * @return Returns job details instance
     */
    public JobDetail createJob(JobKey key, JobDefinition jobDefinition){
        
        // try resolve class 
        var jobClass = this.getJobClass(jobDefinition.getType());

        // check if available
        if(jobClass == null){
            log.error("WorkerFactory: Unknown job class type for the job type: " + jobDefinition.getType());
            return null;
        }
        
        // instantiate a job to schedule 
        var job = JobBuilder.newJob(jobClass)
                .withIdentity(key)
                .storeDurably(false)
                .build();
        
        return job;
    }
    
    /**
     * Creates the job corresponding to the definition
     * 
     * @param job The job to init
     * @param definition The job definition 
     * @return Returns job details instance
     */
    public JobDetail initJob(JobDetail job, JobDefinition definition){

        // the data map
        var systemData = new HashMap<String, Object>();
        
        // populte system data
        systemData.put(JobConstants.PAYLOAD_JOB_ID, definition.getId());
        systemData.put(JobConstants.PAYLOAD_JOB_CODE, definition.getCode());
        systemData.put(JobConstants.PAYLOAD_JOB_GROUP, definition.getGroup());
        systemData.put(JobConstants.PAYLOAD_JOB_TYPE, definition.getType());
        systemData.put(JobConstants.PAYLOAD_JOB_TENANT, definition.getTenant());
        systemData.put(JobConstants.PAYLOAD_JOB_STATUS, definition.getStatus());
        systemData.put(JobConstants.PAYLOAD_JOB_CLUSTER, definition.getCluster());
        systemData.put(JobConstants.PAYLOAD_JOB_EXECUTION, definition.getExecution());
        systemData.put(JobConstants.PAYLOAD_JOB_CREATED, definition.getCreated());
        systemData.put(JobConstants.PAYLOAD_JOB_MODIFIED, definition.getModified());
        
        // add all system data
        job.getJobDataMap().putAll(systemData);
        
        // check if there is payload in job definition populate in data
        if(definition.getPayload() != null){
            // add all payload data
            job.getJobDataMap().putAll(definition.getPayload());
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
     * The trigger definition
     * 
     * @param trigger The trigger definition
     * @return Returns a unique key for trigger
     */
    private String triggerKey(TriggerDefinition trigger){
        // return unique name for trigger
        return Str.blank(trigger.getName()) ? Str.random(8) : trigger.getName();
    }
    
    /**
     * Creates the set of Cron triggers for the given job
     * 
     * @param jobDefinition The job definition
     * @param trigger The trigger definition
     * @return Returns job triggers
     */
    private Set<Trigger> cronTrigger(JobDefinition jobDefinition, TriggerDefinition trigger){

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
            .withIdentity(this.triggerKey(trigger), JobOps.identity(jobDefinition))
            .withSchedule(schedule);

        // if start time is given
        if(trigger.getStartAt() != null){
            triggerBuilder.startAt(Zdt.toDate(trigger.getStartAt()));
        }
        
        // if end time is given
        if(trigger.getEndAt() != null){
            triggerBuilder.endAt(Zdt.toDate(trigger.getEndAt()));
        }

        // schedule job with cron trigger
        result.add(triggerBuilder.build());
        
        return result;
    }
    
    /**
     * Creates the set of Cron triggers for the given job
     * 
     * @param jobDefinition The job definition
     * @param trigger The trigger definition
     * @return Returns job triggers
     */
    private Set<Trigger> periodTrigger(JobDefinition jobDefinition, TriggerDefinition trigger){
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
        
        // create trigger
        var triggerBuilder = TriggerBuilder.newTrigger()
                .withIdentity(this.triggerKey(trigger), JobOps.identity(jobDefinition))
                .withSchedule(SimpleScheduleBuilder.repeatSecondlyForever(periodSecond));
        
        // if start time is given
        if(trigger.getStartAt() != null){
            triggerBuilder.startAt(Zdt.toDate(trigger.getStartAt()));
        }
        
        // if end time is given
        if(trigger.getEndAt() != null){
            triggerBuilder.endAt(Zdt.toDate(trigger.getEndAt()));
        }
        
        // add trigger
        result.add(triggerBuilder.build());
        
        return result;
    }
    
    /**
     * Creates the set of Cron triggers for the given job
     * 
     * @param jobDefinition The job definition
     * @param trigger The trigger definition
     * @return Returns job triggers
     */
    public Set<Trigger> createOneTimeTriggers(JobDefinition jobDefinition, TriggerDefinition trigger){
        // job triggers
        var result = new HashSet<Trigger>();
      
        // create trigger
        var triggerBuilder = TriggerBuilder.newTrigger()
                .withIdentity(this.triggerKey(trigger), JobOps.identity(jobDefinition));
        
         // if start time is given
        if(trigger.getStartAt()!= null){
            triggerBuilder.startAt(Zdt.toDate(trigger.getStartAt()));
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
     * @param jobDefinition The job definition
     * @return Returns job triggers
     */
    public Set<Trigger> createTriggers(JobDefinition jobDefinition){
        
        // triggers to traverse
        var triggers = jobDefinition.getTriggers();
        
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
                    quartzTriggers = this.cronTrigger(jobDefinition, trigger);
                    break;
                case STATIC_PERIOD:
                    quartzTriggers = this.periodTrigger(jobDefinition, trigger);
                    break;
                case ONE_TIME:
                    quartzTriggers = this.createOneTimeTriggers(jobDefinition, trigger);
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
    
    /**
     * Gets the job class type
     * 
     * @param type The type of job
     * @return Returns class for job
     */
    protected Class getJobClass(String type){
        return this.jobClasses.getOrDefault(type, null);
    }
}
