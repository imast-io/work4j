package io.imast.work4j.worker.controller;

import io.imast.core.Coll;
import io.imast.core.Lang;
import io.imast.core.Str;
import io.imast.work4j.channel.SchedulerChannel;
import io.imast.work4j.model.exchange.JobMetadataRequest;
import io.imast.work4j.worker.WorkerFactory;
import io.imast.work4j.worker.WorkerConfiguration;
import io.vavr.control.Try;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.quartz.impl.matchers.GroupMatcher;

/**
 * The polling based worker listener
 * 
 * @author davitp
 */
public class WorkerPollingListener implements WorkerListener {

    /**
     * The worker factory
     */
    protected final WorkerFactory factory;
    
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
     * @param factory The worker factory
     * @param channel The polling channel
     * @param config The worker configuration
     */
    public WorkerPollingListener(WorkerFactory factory, SchedulerChannel channel, WorkerConfiguration config){
        this.factory = factory;
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
        if(!this.config.isSupervise()){
            return;
        }
        
        this.asyncExecutor.scheduleAtFixedRate(() -> this.sync(), 0, this.config.getJobSyncRate().toMillis(), TimeUnit.MILLISECONDS);

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
     * Sync from controller
     */
    protected void syncImpl(){
        
        // get metadata for cluster
        var metadata = this.channel.metadata(new JobMetadataRequest(this.cluster)).orElse(null);
        
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
        var types = this.factory.getJobClasses().keySet();
        
        // for every (group, type) pair do sync process
        Coll.doubleForeach(groups, types, this::syncGroupImpl);
    }
}
