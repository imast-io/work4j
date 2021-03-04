package io.imast.work4j.worker.controller;

import io.imast.work4j.channel.SchedulerChannel;
import io.imast.work4j.channel.worker.WorkerExecutionCompleted;
import io.imast.work4j.channel.worker.WorkerExecutionCreated;
import io.imast.work4j.channel.worker.WorkerExecutionPaused;
import io.imast.work4j.channel.worker.WorkerExecutionResumed;
import io.imast.work4j.channel.worker.WorkerListener;
import io.imast.work4j.channel.worker.WorkerMessage;
import io.imast.work4j.model.cluster.ClusterWorker;
import io.imast.work4j.model.execution.ExecutionIndexEntry;
import io.imast.work4j.model.execution.ExecutionStatus;
import io.imast.work4j.worker.WorkerConfiguration;
import io.imast.work4j.worker.WorkerException;
import io.imast.work4j.worker.instance.QuartzInstance;
import io.imast.work4j.worker.instance.ExecutionKey;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

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
    protected final ClusterWorker worker;

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
     * The execution chunk size to load once
     */
    protected final int executionChunkSize;
    
    /**
     * The scheduler channel
     * 
     * @param worker The worker instance
     * @param instance The quartz instance
     * @param channel The polling channel
     * @param config The worker configuration
     */
    public PollingWorkerListener(ClusterWorker worker, QuartzInstance instance, SchedulerChannel channel, WorkerConfiguration config){
        this.worker = worker;
        this.instance = instance;
        this.channel = channel;
        this.config = config;
        this.asyncExecutor = Executors.newScheduledThreadPool(1);
        this.consumers = new LinkedList<>();
        this.executionChunkSize = 100;
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
        
        // get metadata for cluster
        this.channel.executionIndex(this.worker.getCluster()).subscribe(
                this::syncIndex, 
                err -> log.error("PollingListener: Could not pull execution index.", err));
    }
    
    /**
     * Raise update message
     * 
     * @param message The message to raise
     */
    protected void raise(WorkerMessage message){
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
        if(entries == null){
            return;
        }

        // build map out of index entries
        var index = entries.stream().collect(Collectors.toMap(e -> new ExecutionKey(e.getId(), e.getJobId()), e -> e));
        
        // the keys of index
        var indexKeys = index.keySet();
        
        // the set of paused jobs
        var paused = this.instance.getPausedExecutions();
        
        // the set of executions
        var all = this.instance.getExecutions();
        
        // missing execution keys 
        var toAdd = new HashSet<ExecutionKey>();
        
        // collect all entities that needs to be deleted
        var toDelete = new HashSet<ExecutionKey>();
        
        // entries to pause
        var toPause = new HashSet<ExecutionKey>();
        
        // entries to resume
        var toResume = new HashSet<ExecutionKey>();
        
        // check all the index entries
        indexKeys.forEach(idx -> {
        
            // if any item is in index but not running/paused currently then consider missing
            if(!all.contains(idx)){
                toAdd.add(idx);
                return;
            }
            
            // get the entry
            var entry = index.get(idx);
            
            // item is completed need to remove
            if(entry.getStatus() == ExecutionStatus.COMPLETED){
                toDelete.add(idx);
                return;
            }
            
            // if index says entry should be paused but its not paused do it
            if(entry.getStatus() == ExecutionStatus.PAUSED && !paused.contains(idx)){
                toPause.add(idx);
                return;
            }
            
            // if index says entry should be active but its paused, activate it
            if(entry.getStatus() == ExecutionStatus.ACTIVE && paused.contains(idx)){
                toResume.add(idx);
            }
        });
        
        // check all currently existing items
        all.forEach(existing -> {
        
            // if entry is running but index does not contain info about it delete
            if(!index.containsKey(existing)){
                toDelete.add(existing);
            } 
        });
        
        log.info(String.format("PollingWorkerListener: Current: Index(%s), All(%s), Paused(%s)", index.size(), all.size(), paused.size()));
        
        // if there is nothing to do as per change comparison
        if(toAdd.size() + toDelete.size() + toPause.size() + toResume.size() == 0){
            return;
        }
        
        log.info(String.format("PollingWorkerListener: Changes: Add(%s), Delete(%s), Pause(%s), Resume(%s)", toAdd.size(), toDelete.size(), toPause.size(), toResume.size()));

        // raise completed events
        toDelete.forEach(key -> this.raise(new WorkerExecutionCompleted(key.getExecutionId(), key.getJobId())));
        
        // raise pause events
        toPause.forEach(key -> this.raise(new WorkerExecutionPaused(key.getExecutionId(), key.getJobId())));
        
        // raise resume events
        toResume.forEach(key -> this.raise(new WorkerExecutionResumed(key.getExecutionId(), key.getJobId())));
        
        // the portions of jobs to load
        var portions = new ArrayList<ArrayList<String>>();
        
        // first portion
        portions.add(new ArrayList<>());
        
        // for each adding item break into its portion
        toAdd.forEach(key -> {
            
            // add a portion if previous is full
            if(portions.get(portions.size() - 1).size() >= this.executionChunkSize){
                portions.add(new ArrayList<>());
            }
            
            // insert into last portion
            portions.get(portions.size() - 1).add(key.getExecutionId());
        });
        
        // load missing elements for each portion
        portions.forEach(portion -> {
            
            // nothing to do with empty portions
            if(portion == null || portion.isEmpty()){
                return;
            }
            
            // load portion
            var load = this.channel.executions(portion);
            
            // on completion generate all required messages
            load.subscribe(
                response -> {
                    
                    // nothing to do, no result
                    if(response == null || response.isEmpty()){
                        return;
                    }
                    
                    response.forEach(exec -> this.raise(new WorkerExecutionCreated(exec)));
                    
                }, 
                error ->  log.error("PollingListener: Could not load portion of executions", error)
            );
        });
    }
}
