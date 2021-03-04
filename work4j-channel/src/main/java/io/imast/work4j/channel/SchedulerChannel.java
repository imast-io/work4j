package io.imast.work4j.channel;

import io.imast.work4j.model.cluster.ClusterWorker;
import io.imast.work4j.model.iterate.*;
import io.imast.work4j.model.execution.CompletionSeverity;
import io.imast.work4j.model.execution.ExecutionIndexEntry;
import io.imast.work4j.model.execution.JobExecution;
import io.imast.work4j.model.cluster.WorkerHeartbeat;
import io.imast.work4j.model.cluster.WorkerJoinInput;
import java.util.List;
import reactor.core.publisher.Mono;

/**
 * The worker channel interface 
 * 
 * @author davitp
 */
public interface SchedulerChannel {
    
    /**
     * Pull job groups for the given cluster
     * 
     * @param cluster The target cluster
     * @return Returns execution index entries
     */
    public Mono<List<ExecutionIndexEntry>> executionIndex(String cluster);
    
    /**
     * Exchange current status with modified entries
     * 
     * @param ids The executions request ids
     * @return Returns executions response
     */
    public Mono<List<JobExecution>> executions(List<String> ids);
    
    /**
     * Completes the job execution in scheduler
     * 
     * @param id The identifier of job execution
     * @param severity The severity of completion
     * @return Returns updated job execution
     */
    public Mono<JobExecution> complete(String id, CompletionSeverity severity);
    
    /**
     * Adds iteration information to scheduler
     * 
     * @param iteration The iteration to register
     * @return Returns registered iteration
     */
    public Mono<Iteration> iterate(IterationInput iteration);
    
    /**
     * Registers worker into the scheduler
     * 
     * @param input The worker input to register
     * @return Returns registered worker
     */
    public Mono<ClusterWorker> registration(WorkerJoinInput input);
    
    /**
     * Send a Heartbeat signal to from worker scheduler
     * 
     * @param heartbeat The worker reported heartbeat
     * @return Returns updated agent definition
     */
    public Mono<ClusterWorker> heartbeat(WorkerHeartbeat heartbeat);
}
