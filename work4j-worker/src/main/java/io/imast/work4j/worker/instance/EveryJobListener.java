package io.imast.work4j.worker.instance;

import io.imast.core.Zdt;
import io.imast.work4j.channel.SchedulerChannel;
import io.imast.work4j.model.iterate.IterationStatus;
import io.imast.work4j.model.iterate.JobIteration;
import io.imast.work4j.worker.JobOps;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;

/**
 * The base job listener 
 * 
 * @author davitp
 */
@Slf4j
public class EveryJobListener implements JobListener {

    /**
     * The scheduler channel
     */
    protected final SchedulerChannel schedulerChannel;
    
    /**
     * Creates new instance of Every Job Listener
     * 
     * @param schedulerChannel The worker channel
     */
    public EveryJobListener(SchedulerChannel schedulerChannel) {
        this.schedulerChannel = schedulerChannel;   
    }
    
    /**
     * Gets name of job listener
     * 
     * @return Returns job listener name
     */
    @Override
    public String getName() {
        return "WORK4J_JOB_LISTENER";
    }

    /**
     * The job is about to be executed
     * 
     * @param context The job execution context
     */
    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
    }

    /**
     * The job execution is vetoed by trigger listener
     * 
     * @param context The job execution context
     */
    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
    }

    /**
     * The job was executed 
     * 
     * @param context The job execution context
     * @param jobException The job exception
     */
    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {

        // get the job definition
        var definition = JobOps.getJobDefinition(context);
        
        // check if definition is null
        if(definition == null){
            return;
        }
        
        // the job id
        var jobId = definition.getId();
        
        // the job status
        var status = jobException == null ? IterationStatus.SUCCESS : IterationStatus.FAILURE;
        
        // if silent reporting 
        var silent = definition.getExecution() != null && definition.getExecution().isSilentIterations();
        
        // if silent reporting is enabled will just silently skip iteration report
        if(silent){
            return;
        }
        
        // the job run time
        var runtime = context.getJobRunTime();
        
        // create iteration entity
        var iteration = JobIteration.builder()
                .id(null)
                .jobId(jobId)
                .runtime(runtime)
                .status(status)
                .message(jobException == null ? null : jobException.toString())
                .timestamp(Zdt.utc())
                .build();
        
        // register iteration and get the result
        var result = this.schedulerChannel.iterate(iteration);
        
        if(!result.isPresent()){
            log.warn(String.format("EveryJobListener: Could not register %s iteration for job %s (%s)", iteration.getStatus(), definition.getCode(), definition.getGroup()));
        }
    }    
}
