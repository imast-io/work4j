package io.imast.work4j.worker.instance;

import io.imast.work4j.channel.SchedulerChannel;
import io.imast.work4j.model.execution.CompletionSeverity;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.SchedulerListener;
import org.quartz.Trigger;
import org.quartz.TriggerKey;

/**
 * The trigger listener
 * 
 * @author davitp
 */
@Slf4j
public class JobSchedulerListener implements SchedulerListener {
    
    /**
     * The worker channel for controller communication
     */
    protected final SchedulerChannel schedulerChannel;
    
    /**
     * Creates new instance of Every Job Listener
     * 
     * @param schedulerChannel The scheduler channel 
     */
    public JobSchedulerListener(SchedulerChannel schedulerChannel) {
        this.schedulerChannel = schedulerChannel;
    }

    /**
     * The job is scheduled
     * 
     * @param trigger The trigger
     */
    @Override
    public void jobScheduled(Trigger trigger) {
    }

    /**
     * The job is unscheduled
     * 
     * @param triggerKey The trigger key
     */
    @Override
    public void jobUnscheduled(TriggerKey triggerKey) {
    }

    /**
     * The trigger is finalized
     * 
     * @param trigger The trigger
     */
    @Override
    public void triggerFinalized(Trigger trigger) {
        
        // update job and get 
        // FIXME
        this.schedulerChannel.complete(trigger.getJobKey().getName(), CompletionSeverity.SUCCESS).subscribe();
    }

    /**
     * The trigger is paused
     * 
     * @param triggerKey The trigger key
     */
    @Override
    public void triggerPaused(TriggerKey triggerKey) {
    }

    /**
     * The triggers are paused
     * 
     * @param triggerGroup The trigger group
     */
    @Override
    public void triggersPaused(String triggerGroup) {
    }

    /**
     * The trigger is resumed
     * 
     * @param triggerKey The trigger key
     */
    @Override
    public void triggerResumed(TriggerKey triggerKey) {
    }
    
    /**
     * The triggers are resumed
     * 
     * @param triggerGroup The trigger group
     */
    @Override
    public void triggersResumed(String triggerGroup) {
    }

    /**
     * The job is added
     * 
     * @param jobDetail The job details 
     */
    @Override
    public void jobAdded(JobDetail jobDetail) {
    }

    /**
     * The job is deleted
     * 
     * @param jobKey The job key
     */
    @Override
    public void jobDeleted(JobKey jobKey) {       
    }

    /**
     * The job is paused
     * 
     * @param jobKey The job key
     */
    @Override
    public void jobPaused(JobKey jobKey) {
    }

    /**
     * The job group is paused
     * 
     * @param jobGroup The job group
     */
    @Override
    public void jobsPaused(String jobGroup) {
    }

    /**
     * The job is resumed
     * 
     * @param jobKey The job key
     */
    @Override
    public void jobResumed(JobKey jobKey) {
    }

    /**
     * The job group is resumed 
     * 
     * @param jobGroup The job group
     */
    @Override
    public void jobsResumed(String jobGroup) {
    }

    /**
     * The scheduler error happened
     * 
     * @param msg The message
     * @param cause The cause
     */
    @Override
    public void schedulerError(String msg, SchedulerException cause) {
    }

    /**
     * The scheduler is in standby mode
     */
    @Override
    public void schedulerInStandbyMode() {
    }

    /**
     * The scheduler started
     */
    @Override
    public void schedulerStarted() {
    }

    /**
     * The scheduler is starting
     */
    @Override
    public void schedulerStarting() {
    }

    /**
     * The scheduler shutdown
     */
    @Override
    public void schedulerShutdown() {
    }

    /**
     * The scheduler is shutting down
     */
    @Override
    public void schedulerShuttingdown() {
    }

    /**
     * The scheduling data is cleared
     */
    @Override
    public void schedulingDataCleared() {
    }
}
