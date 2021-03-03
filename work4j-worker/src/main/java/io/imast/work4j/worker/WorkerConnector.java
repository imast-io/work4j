package io.imast.work4j.worker;

import io.imast.core.Lang;
import io.imast.core.Str;
import io.imast.work4j.channel.SchedulerChannel;
import io.imast.work4j.model.cluster.Worker;
import io.imast.work4j.model.cluster.WorkerInput;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;

/**
 * The worker connector module
 * 
 * @author davitp
 */
@Slf4j
public class WorkerConnector {

    /**
     * The scheduler channel
     */
    protected final SchedulerChannel channel;
   
    /**
     * Creates new instance of worker connector
     * 
     * @param channel 
     */
    public WorkerConnector(SchedulerChannel channel){
       this.channel = channel; 
    }
    
    /**
     * Connects to the scheduler based on the channel and gets instance of worker
     * 
     * @param config The configuration of worker
     * @return Returns worker instance
     * @throws WorkerException 
     */
    public Worker connect(WorkerConfiguration config) throws WorkerException{
        
        // get the given name
        var name = config.getName();
        
        // if name is not given use a random name
        if(Str.blank(name)){
            name = Str.random(8);
        }
        
        // get the cluster name (use default if not given from configuration)
        var cluster = Str.blank(config.getCluster()) ? JobConstants.DEFAULT_CLUSTER : config.getCluster();
        
        // build the worker input
        var input = WorkerInput.builder()
                .name(name)
                .cluster(cluster)
                .tenant(config.getTenant())
                .maxIdle(config.getHeartbeatRate())
                .build();
        
        // number of tries
        var tries = config.getWorkerRegistrationTries();
        
        // by default try N times
        if(tries == null || tries <= 0){
            tries = 3;
        }
        
        // hold for last known exception
        Throwable lastException = null;
        
        // try several times
        for(int i = 0; i < tries; ++i){
            
            // try register
            var result = Try.of(() -> this.channel.registration(input).block());
        
            // if successfuly registered
            if(result.isSuccess()){
                return result.get();
            }
            
            // save last known exception
            lastException = result.getCause();
            
            // log about the error
            log.error("WorkerConnector: Could not connect to register worker: " + result.getCause().getMessage());
            
            // delay for the next try
            Lang.wait(5000);
        }
        
        throw new WorkerException("Could not connect to register the worker", lastException);
    }
}
