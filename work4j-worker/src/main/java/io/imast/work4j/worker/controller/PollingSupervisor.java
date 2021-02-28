package io.imast.work4j.worker.controller;

import io.imast.core.Coll;
import io.imast.core.Lang;
import io.imast.core.Str;
import io.imast.work4j.channel.SchedulerChannel;
import io.imast.work4j.model.exchange.JobMetadataRequest;
import io.imast.work4j.worker.WorkerConfiguration;
import io.imast.work4j.worker.WorkerException;
import io.imast.work4j.worker.instance.QuartzInstance;
import io.vavr.control.Try;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;

/**
 * The polling based worker listener
 * 
 * @author davitp
 */
@Slf4j
public class PollingSupervisor implements WorkerSupervior {

    /**
     * The quartz instance
     */
    protected final QuartzInstance instance;
    
    /**
     * The scheduler channel
     */
    protected final SchedulerChannel channel;
    
    /**
     * The scheduler channel
     */
    protected final WorkerConfiguration config;
    
    /**
     * The asynchronous executor
     */
    protected final ScheduledExecutorService asyncExecutor;

    /**
     * The set of consumers
     */
    protected final LinkedList<Consumer<WorkerUpdateMessage>> consumers;
    
    /**
     * The scheduler channel
     * 
     * @param instance The quartz instance
     * @param channel The polling channel
     * @param config The worker configuration
     */
    public PollingSupervisor(QuartzInstance instance, SchedulerChannel channel, WorkerConfiguration config){
        this.instance = instance;
        this.channel = channel;
        this.config = config;
        this.asyncExecutor = Executors.newScheduledThreadPool(1);
        this.consumers = new LinkedList<>();
    }
    
    /**
     * Starts the listener
     */
    @Override
    public void start() {
        
        // only if should supervise
        if(this.config.getPollingRate() == null || this.config.getPollingRate() == 0){
            return;
        }
        
        this.asyncExecutor.scheduleAtFixedRate(() -> this.sync(), 0, this.config.getPollingRate(), TimeUnit.MILLISECONDS);
    }

    /**
     * Add a given consumer
     * 
     * @param consumer The consumer to add
     */
    @Override
    public void add(Consumer<WorkerUpdateMessage> consumer) {
        this.consumers.add(consumer);
    }

    /**
     * Remove the given consumer
     * 
     * @param consumer The consumer to remove
     */
    @Override
    public void remove(Consumer<WorkerUpdateMessage> consumer) {
        this.consumers.removeLastOccurrence(consumer);
    }

    /**
     * Stop the listener
     */
    @Override
    public void stop() {
        this.asyncExecutor.shutdown();
    }   
    
    /**
     * Refresh jobs and sync with server
     * 
     */
    protected void sync(){
        
        try {
            this.syncImpl();
        }
        catch(WorkerException error){
            log.error(String.format("PollingSupervisor: Could not sync jobs, Error: %s", error.getLocalizedMessage()), error);
        }
    }
    
    /**
     * Sync from controller
     * 
     * @throws io.imast.work4j.worker.WorkerException
     */
    protected void syncImpl() throws WorkerException{
        
        // get metadata for cluster
        var metadata = this.channel.metadata(new JobMetadataRequest(this.instance.getCluster())).orElse(null);
        
        // check if no groups
        if(metadata == null){
            throw new WorkerException("PollingSupervisor: Could not pull metadata from controller.");
        }

        // get groups
        var groups = new HashSet<>(Lang.or(metadata.getGroups(), Str.EMPTY_LIST));
        
        // get running groups
        var runningGroups = Try.of(() -> this.instance.getGroups()).getOrElse(Str.EMPTY_LIST);
        
        // unschedule all jobs in groups if the groups is not in controller
        runningGroups.forEach(running -> {
            
            // leave group as it is running both in controller and in worker
            if(groups.contains(running)){
                return;
            }
            
            // for each job in group raise unschedule uperation
            Try.of(() -> this.instance.getJobs(running))
                    .getOrElse(Set.of())
                    .forEach(code -> this.raise(new WorkerUpdateMessage(UpdateOperation.REMOVE, code, running, null)));
        });
        
        // types of jobs
        var types = this.instance.getTypes();
        
        // for every (group, type) pair do sync process
        Coll.doubleForeach(groups, types, (group, type) -> {
            try{
                this.syncGroupImpl(group, type);
            }
            catch(WorkerException ex){
                log.warn("PollingSupervisor: Something went wrong while syncing jobs. " + ex.getMessage());
            }
        });
    }
    
    /**
     * Sync with controller for group and type pair
     * 
     * @param group The target group
     * @param type The target type
     * @throws io.imast.work4j.worker.WorkerException
     */
    protected void syncGroupImpl(String group, String type) throws WorkerException{
        
        // get job list
        var statusUpdate = this.channel.statusExchange(this.instance.getStatus(group, type)).orElse(null);

        // handle if not recieved jobs
        if(statusUpdate == null){
            throw new WorkerException("PollingSupervisor: Did not get proper response from scheduler.");
        }

        log.debug(String.format("PollingSupervisor: Syncing jobs in %s with server. Deleted: %s, Updated: %s, Added: %s", group, statusUpdate.getRemoved().size(), statusUpdate.getUpdated().size(), statusUpdate.getAdded().size()));

        // unschedule all the removed jobs
        statusUpdate.getRemoved().forEach((removedJob) -> {
            this.raise(new WorkerUpdateMessage(UpdateOperation.REMOVE, removedJob, statusUpdate.getGroup(), null));
        });

        // schedule added jobs
        statusUpdate.getAdded().values().forEach(job -> {
            this.raise(new WorkerUpdateMessage(UpdateOperation.ADD, job.getCode(), job.getGroup(), job));
        });
        
        // schedule updated jobs
        statusUpdate.getUpdated().values().forEach(job -> {
            this.raise(new WorkerUpdateMessage(UpdateOperation.UPDATE, job.getCode(), job.getGroup(), job));
        });
    }
    
    /**
     * Raise update message
     * 
     * @param message The message to raise
     */
    protected void raise(WorkerUpdateMessage message){
        this.consumers.forEach(consumer -> consumer.accept(message));
    }
}
