package io.imast.work4j.worker.controller;

import io.imast.core.Coll;
import io.imast.core.Lang;
import io.imast.core.Str;
import io.imast.core.Zdt;
import io.imast.work4j.worker.JobConstants;
import io.imast.work4j.worker.WorkerException;
import io.vavr.control.Try;
import java.time.Duration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import java.util.UUID;

/**
 * The job executor manager
 * 
 * @author davitp
 */
@Slf4j
public class WorkerControllerOld {
        
    /**
     * The job manager configuration
     */
    protected final WorkerControllerConfig config;
    
    /**
     * The job factory
     */
    protected final JobFactory jobFactory;
    
    /**
     * The worker channel
     */
    protected final WorkerChannel workerChannel;
    
    /**
     * The scheduler executor to sync jobs and exchange metadata
     */
    protected final ScheduledExecutorService schedulerExecutor;
    
    /**
     * The cluster name
     */
    protected final String cluster;
    
    /**
     * The worker name
     */
    protected final String worker;
    
    /**
     * The scheduler
     */
    protected Scheduler scheduler;
    
    /**
     * The quartz scheduler factory
     */
    protected StdSchedulerFactory schedulerFactory;
    
    /**
     * The agent definition
     */
    protected AgentDefinition agentDefinition;
    
    /**
     * The job executor module 
     * 
     * @param config The job manager configuration
     * @param jobFactory The job factory
     * @param workerChannel The worker channel
     */
    public WorkerControllerOld(WorkerControllerConfig config, JobFactory jobFactory, WorkerChannel workerChannel){
        this.config = config;
        this.jobFactory = jobFactory;
        this.workerChannel = workerChannel;
        this.cluster = Str.blank(this.config.getCluster()) ? JobConstants.DEFAULT_CLUSTER : this.config.getCluster();
        this.worker = Str.blank(this.config.getWorker()) ? UUID.randomUUID().toString() : this.config.getWorker();
    }
    
    /**
     * Initialize the scheduling manager
     * @throws io.imast.work4j.worker.WorkerException
     */
    public void initialize() throws WorkerException {
        
       
        // initialize scheduler context
        this.initializeContext();        
    }
    
    /**
     * Execute the job manager
     * 
     * @throws io.imast.work4j.worker.WorkerException
     */
    public void execute() throws WorkerException{
        
        // the agent definition
        this.agentDefinition = this.ensureRegister(100);
        
        // register agent (try N times)
        if(this.agentDefinition == null){
            throw new WorkerException("WorkerController: Could not register agent in the system");
        }
        
        log.debug(String.format("WorkerController: Agent %s is successfuly registered.", this.agentDefinition.getId()));
        
        // schedule heartbit reporter
        this.schedulerExecutor.scheduleAtFixedRate(() -> this.heartbeat(), 0, this.config.getWorkerSignalRate().toMillis(), TimeUnit.MILLISECONDS);
        
        // if supervisor then schedule sync jobs
        if(this.config.isSupervise()){  
            // start job sync worker
            this.schedulerExecutor.scheduleAtFixedRate(() -> this.sync(), 0, this.config.getJobSyncRate().toMillis(), TimeUnit.MILLISECONDS);
        }
       
        try{
            this.scheduler.start();
        }
        catch(SchedulerException error){
            log.error("WorkerChannel: Could not start quartz scheduler: ", error);
        }        
    }
    
    /**
     * Refresh jobs and sync with server
     * 
     */
    public void sync(){
        
        try {
            this.syncImpl();
        }
        catch(Throwable error){
            log.error(String.format("WorkerController: Could not sync jobs, Error: %s", error.getLocalizedMessage()), error);
        }
    }
    
    

    /**
     * Sync with controller for group and type pair
     * 
     * @param group The target group
     * @param type The target type
     */
    protected void syncGroupImpl(String group, String type){
        
        // get job list
        var statusUpdate = this.workerChannel.statusExchange(this.status(group, type)).orElse(null);

        // handle if not recieved jobs
        if(statusUpdate == null){
            log.warn("WorkerController: Did not get proper response from scheduler.");
            return;
        }

        log.debug(String.format("WorkerController: Syncing jobs in %s with server. Deleted: %s, Updated: %s, Added: %s", group, statusUpdate.getRemoved().size(), statusUpdate.getUpdated().size(), statusUpdate.getAdded().size()));

        // unschedule all the removed jobs
        statusUpdate.getRemoved().forEach((removedJob) -> {
            this.unschedule(removedJob, statusUpdate.getGroup());
        });

        // schedule added jobs
        statusUpdate.getAdded().values().forEach(this::schedule);

        // reschedule updated jobs
        statusUpdate.getUpdated().values().forEach(this::reschedule);
    }
}
