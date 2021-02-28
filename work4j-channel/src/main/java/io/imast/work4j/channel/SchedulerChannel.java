package io.imast.work4j.channel;

import io.imast.work4j.model.iterate.*;
import io.imast.work4j.model.execution.CompletionSeverity;
import io.imast.work4j.model.execution.ExecutionIndexRequest;
import io.imast.work4j.model.execution.ExecutionIndexResponse;
import io.imast.work4j.model.execution.ExecutionsRequest;
import io.imast.work4j.model.execution.ExecutionsResponse;
import io.imast.work4j.model.execution.JobExecution;
import io.imast.work4j.model.worker.Worker;
import io.imast.work4j.model.worker.WorkerHeartbeat;
import io.imast.work4j.model.worker.WorkerInput;
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
     * @param request The request structure of executions
     * @return Returns execution index entries
     */
    public Mono<ExecutionIndexResponse> executionIndex(ExecutionIndexRequest request);
    
    /**
     * Exchange current status with modified entries
     * 
     * @param request The executions request 
     * @return Returns executions response
     */
    public Mono<ExecutionsResponse> executions(ExecutionsRequest request);
    
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
    public Mono<Worker> registration(WorkerInput input);
    
    /**
     * Send a Heartbeat signal to from worker scheduler
     * 
     * @param id The identifier of worker instance
     * @param heartbeat The worker reported heartbeat
     * @return Returns updated agent definition
     */
    public Mono<Worker> heartbeat(String id, WorkerHeartbeat heartbeat);
}
