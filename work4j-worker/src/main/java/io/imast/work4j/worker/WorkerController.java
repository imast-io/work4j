package io.imast.work4j.worker;

import io.imast.core.Coll;
import io.imast.core.Lang;
import io.imast.core.Str;
import io.imast.core.Zdt;
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
public class WorkerController {
        
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
    public WorkerController(WorkerControllerConfig config, JobFactory jobFactory, WorkerChannel workerChannel){
        this.config = config;
        this.jobFactory = jobFactory;
        this.workerChannel = workerChannel;
        this.schedulerExecutor =  Executors.newScheduledThreadPool(1);
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
     * Sync from controller
     */
    protected void syncImpl(){
        
        // get metadata for cluster
        var metadata = this.workerChannel.metadata(new JobMetadataRequest(this.cluster)).orElse(null);
        
        // check if no groups
        if(metadata == null){
            throw new RuntimeException("WorkerController: Could not pull metadata from controller.");
        }

        // get groups
        var groups = new HashSet<>(Lang.or(metadata.getGroups(), Str.EMPTY_LIST));
        
        // get running groups
        var runningGroups = Try.of(() -> this.scheduler.getJobGroupNames()).getOrElse(Str.EMPTY_LIST);
        
        // unschedule all jobs in groups if the groups is not in controller
        runningGroups.forEach(running -> {
            
            // leave group as it is running both in controller and in worker
            if(groups.contains(running)){
                return;
            }
            
            // get jobs in group
            var jobKeys = Try.of(() -> this.scheduler.getJobKeys(GroupMatcher.jobGroupEquals(running))).getOrElse(Set.of());
            
            // unschedule
            jobKeys.forEach(job -> this.unschedule(job.getName(), job.getGroup()));
        });
        
        // types of jobs
        var types = this.jobFactory.getJobClasses().keySet();
        
        // for every (group, type) pair do sync process
        Coll.doubleForeach(groups, types, this::syncGroupImpl);
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
    
    /**
     * Ensures that agent client has been registered
     * 
     * @param tryCount Number of tries
     * @return Returns true if successful
     */
    private AgentDefinition ensureRegister(int tryCount){
        
        // try several times
        for(int i = 0; i < tryCount; ++i){
            
            // register
            var agent = this.register();
            
            // if successfuly registered
            if(agent != null){
                return agent;
            }
            
            // delay for the next try
            Lang.wait(5000);
        }
        
        return null;
    }
    
    /**
     * Register itself to the scheduler
     */
    private AgentDefinition register(){
        
        // the agent signal rate
        Duration singalRate = this.config.getWorkerSignalRate();
        
        // now time
        var now = Zdt.utc();
        
        // worker at cluster identity
        var identity = String.format("%s@%s", this.worker, this.cluster);
        
        // the agent definition
        var agent = AgentDefinition.builder()
                .id(identity)
                .worker(this.worker)
                .cluster(this.cluster)
                .name(identity)
                .supervisor(this.config.isSupervise())
                .health(new AgentHealth(now, AgentActivityType.REGISTER))
                .expectedSignalMinutes(singalRate.toSeconds() / 60.0)
                .registered(now)
                .build();
        
        return this.workerChannel.registration(agent).orElse(null);
    }

    /**
     * Report the health to scheduler
     */
    private void heartbeat() {
                
        // new health info
        var health = new AgentHealth(Zdt.utc(), AgentActivityType.HEARTBEAT);
        
        this.workerChannel.heartbeat(this.agentDefinition.getId(), health);
    }

    /**
     * Initialize the context modules
     */
    private void initializeContext() throws WorkerException {
        
        // check if scheduler is given
        if(this.scheduler == null){
            return;
        }
        
        this.initContextModules();
        this.initListeners();
    }
}
