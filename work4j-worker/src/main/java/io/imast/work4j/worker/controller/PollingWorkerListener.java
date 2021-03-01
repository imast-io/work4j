package io.imast.work4j.worker.controller;

import io.imast.core.Coll;
import io.imast.core.Lang;
import io.imast.core.Str;
import io.imast.work4j.channel.SchedulerChannel;
import io.imast.work4j.channel.worker.WorkerListener;
import io.imast.work4j.channel.worker.WorkerMessage;
import io.imast.work4j.model.execution.ExecutionIndexEntry;
import io.imast.work4j.model.execution.ExecutionIndexRequest;
import io.imast.work4j.model.worker.Worker;
import io.imast.work4j.worker.WorkerConfiguration;
import io.imast.work4j.worker.WorkerException;
import io.imast.work4j.worker.instance.QuartzInstance;
import io.vavr.control.Try;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;

/**
 * The polling based worker listener
 * 
 * @author davitp
 */
@Slf4j
public class PollingWorkerListener implements WorkerListener {
    
    /**
     * The worker instance
     */
    protected final Worker worker;

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
    protected final LinkedList<Consumer<WorkerMessage>> consumers;
    
    /**
     * The scheduler channel
     * 
     * @param worker The worker instance
     * @param instance The quartz instance
     * @param channel The polling channel
     * @param config The worker configuration
     */
    public PollingWorkerListener(Worker worker, QuartzInstance instance, SchedulerChannel channel, WorkerConfiguration config){
        this.worker = worker;
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
    public void add(Consumer<WorkerMessage> consumer) {
        this.consumers.add(consumer);
    }

    /**
     * Remove the given consumer
     * 
     * @param consumer The consumer to remove
     */
    @Override
    public void remove(Consumer<WorkerMessage> consumer) {
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
            log.error(String.format("PollingListener: Could not sync executions, Error: %s", error.getLocalizedMessage()), error);
        }
    }
    
    /**
     * Sync from controller
     * 
     * @throws io.imast.work4j.worker.WorkerException
     */
    protected void syncImpl() throws WorkerException{
        
        // the index request 
        var indexRequest = ExecutionIndexRequest.builder()
                .cluster(this.worker.getCluster())
                .tenant(this.worker.getTenant())
                .build();
        
        // get metadata for cluster
        this.channel.executionIndex(indexRequest).subscribe(response -> {
            this.syncIndex(response.getEntries()); 
        }, 
        err -> log.error("PollingListener: Could not pull execution index.", err));
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

    /**
     * Do sync operation based on received entries
     * 
     * @param entries The received index entries
     * @throws WorkerException 
     */
    protected void syncIndex(List<ExecutionIndexEntry> entries) throws WorkerException {

        // check if no groups
        if(entries == null || entries.isEmpty()){
            return;
        }

        // build map out of index entries
        var index = entries.stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
        
        // the set of paused jobs
        var paused = this.instance.getPausedExecutions();
        
        // the set of executions
        var all = this.instance.getExecutions();
        
        
        
    }
}
