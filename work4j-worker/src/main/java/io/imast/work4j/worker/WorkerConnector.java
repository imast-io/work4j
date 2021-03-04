package io.imast.work4j.worker;

import io.imast.core.Lang;
import io.imast.core.Str;
import io.imast.work4j.channel.SchedulerChannel;
import io.imast.work4j.model.cluster.ClusterWorker;
import io.imast.work4j.model.cluster.WorkerJoinInput;
import io.imast.work4j.model.cluster.WorkerKind;
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
     * The configuration
     */
    protected final WorkerConfiguration config;
   
    /**
     * Creates new instance of worker connector
     * 
     * @param config The configuration
     * @param channel The scheduler channel
     */
    public WorkerConnector(WorkerConfiguration config, SchedulerChannel channel){
        this.config = config;
        this.channel = channel; 
    }
    
    /**
     * Connects to the scheduler based on the channel and gets instance of worker
     * 
     * @return Returns worker instance
     * @throws WorkerException 
     */
    public ClusterWorker connect() throws WorkerException {
        
        // get the given name
        var name = this.config.getName();
        
        // if name is not given use a random name
        if(Str.blank(name)){
            
            if(this.config.getClusteringType() == ClusteringType.EXCLUSIVE){
                name = "EXCLUSIVE";
            }
            
            if(this.config.getClusteringType() == ClusteringType.BALANCED){
                name = "BALANCED";
            }
            
            if(this.config.getClusteringType() == ClusteringType.REPLICA){
                name = String.format("REPLICA-%s", Str.random(3));
            }
        }
        
        // get the cluster name (use default if not given from configuration)
        var cluster = Str.blank(this.config.getCluster()) ? JobConstants.DEFAULT_CLUSTER : this.config.getCluster();
        
        // the persistence type
        var persistence = this.config.getPersistenceType() == null ? PersistenceType.NO : this.config.getPersistenceType();
        
        // build the worker input
        var input = WorkerJoinInput.builder()
                .name(name)
                .cluster(cluster)
                .session(String.format("%s-%s-%s-%s", Str.random(4), Str.random(4), Str.random(4), Str.random(4)))
                .persistence(persistence != PersistenceType.NO)
                .persistenceMethod(persistence.name())
                .kind(this.mapKind(this.config.getClusteringType()))
                .maxIdle(this.config.getHeartbeatRate())
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
    
    /**
     * Map clustering type to worker kind
     * 
     * @param type The clustering type
     * @return Returns the worker kind
     */
    protected WorkerKind mapKind(ClusteringType type) throws WorkerException {
        
        // map clustering type to worker kind
        switch(type){
            case EXCLUSIVE:
                return WorkerKind.EXCLUSIVE;
            case REPLICA:
                return WorkerKind.REPLICA;
            case BALANCED:
                return WorkerKind.BALANCED;
        }
        
        throw new WorkerException("No Clustering Type is not supported");
    }
}
