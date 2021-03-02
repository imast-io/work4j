package io.imast.work4j.worker.instance;

import io.imast.core.Lang;
import io.imast.work4j.channel.SchedulerChannel;
import io.imast.work4j.model.JobOptions;
import io.imast.work4j.model.iterate.IterationInput;
import io.imast.work4j.model.iterate.IterationStatus;
import io.imast.work4j.model.worker.Worker;
import io.imast.work4j.worker.JobConstants;
import io.imast.work4j.worker.job.JobOps;
import java.util.Date;
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
     * The worker instance
     */
    protected final Worker worker;
    
    /**
     * The scheduler channel
     */
    protected final SchedulerChannel schedulerChannel;
    
    /**
     * Creates new instance of Every Job Listener
     * 
     * @param worker The worker instance
     * @param schedulerChannel The worker channel
     */
    public EveryJobListener(Worker worker, SchedulerChannel schedulerChannel) {
        this.worker = worker;
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

        // the job execution id
        var executionId = JobOps.<String>getValue(context.getJobDetail().getJobDataMap(), JobConstants.PAYLOAD_JOB_EXECUTION_ID);
        
        // the job definition id
        var jobId = JobOps.<String>getValue(context.getJobDetail().getJobDataMap(), JobConstants.PAYLOAD_JOB_DEFINITION_ID);
                
        // the job status
        var status = jobException == null ? IterationStatus.SUCCESS : IterationStatus.FAILURE;
        
        // get execution options
        var options = JobOps.<JobOptions>getValue(context.getJobDetail().getJobDataMap(), JobConstants.PAYLOAD_JOB_OPTIONS);
        
        // if silent reporting 
        var silent = options != null && options.isSilentIterations();
        
        // get result if any
        var output = context.getResult();
        
        // if silent reporting is enabled will just silently skip iteration report
        if(silent){
            return;
        }
        
        // the job run time
        var runtime = context.getJobRunTime();
        
        // create iteration entity
        var iteration = IterationInput.builder()
                .executionId(executionId)
                .jobId(jobId)
                .workerId("FIXME")
                .runtime(runtime)
                .status(status)
                .payload(Lang.safeCast(output))
                .message(jobException == null ? null : jobException.toString())
                .timestamp(new Date())
                .build();
        
        // register iteration and get the result
        this.schedulerChannel.iterate(iteration).subscribe();
    }    
}
