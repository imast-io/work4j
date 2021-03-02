package io.imast.work4j.controller.channel;

import io.imast.work4j.channel.SchedulerChannel;
import io.imast.work4j.controller.SchedulerController;
import io.imast.work4j.model.execution.CompletionSeverity;
import io.imast.work4j.model.execution.ExecutionIndexEntry;
import io.imast.work4j.model.execution.ExecutionUpdateInput;
import io.imast.work4j.model.execution.ExecutionStatus;
import io.imast.work4j.model.execution.ExecutionsResponse;
import io.imast.work4j.model.execution.JobExecution;
import io.imast.work4j.model.iterate.Iteration;
import io.imast.work4j.model.iterate.IterationInput;
import io.imast.work4j.model.worker.Worker;
import io.imast.work4j.model.worker.WorkerHeartbeat;
import io.imast.work4j.model.worker.WorkerInput;
import io.vavr.control.Try;
import java.util.List;
import reactor.core.publisher.Mono;

/**
 * The special scheduler channel for local interaction
 * 
 * @author davitp
 */
public class BundledSchedulerChannel implements SchedulerChannel {
   
    /**
     * The scheduler controller instance
     */
    protected final SchedulerController controller;
    
    /**
     * Creates new instance of bundled scheduler channel
     * 
     * @param controller The target controller
     */
    public BundledSchedulerChannel(SchedulerController controller){
        this.controller = controller;
    }
    
    /**
     * Pull job groups for the given cluster
     * 
     * @param tenant The target tenant
     * @param cluster The target cluster
     * @return Returns execution index entries
     */
    @Override
    public Mono<List<ExecutionIndexEntry>> executionIndex(String tenant, String cluster) {
        // try get executions
        var executions = Try.of(() -> this.controller.getExecutionIndex(tenant, cluster));
        
        // in case of success build and return response
        if(executions.isSuccess()){
            return Mono.just(executions.get());
        }
        
        return Mono.empty();
    }
    
    /**
     * Exchange current status with modified entries
     * 
     * @param ids The executions request ids
     * @return Returns executions response
     */
    @Override
    public Mono<List<JobExecution>> executions(List<String> ids){
        
        // try get executions
        var executions = Try.of(() -> this.controller.getExecutionsByIds(ids));
        
        // in case of success build and return response
        if(executions.isSuccess()){
            return Mono.just(executions.get());
        }
        
        return Mono.empty();
    }
    
    /**
     * Completes the job execution in scheduler
     * 
     * @param id The identifier of job execution
     * @param severity The severity of completion
     * @return Returns updated job execution
     */
    @Override
    public Mono<JobExecution> complete(String id, CompletionSeverity severity){
        
        // do update the execution
        var updated = Try.of(() -> this.controller.updateExecution(id, new ExecutionUpdateInput(ExecutionStatus.COMPLETED, severity)));
        
        // in case of success build and return response
        if(updated.isSuccess()){
            return Mono.just(updated.get());
        }
        
        return Mono.empty();
    }
    
    /**
     * Adds iteration information to scheduler
     * 
     * @param iteration The iteration to register
     * @return Returns registered iteration
     */
    @Override
    public Mono<Iteration> iterate(IterationInput iteration){
        // do create iteration
        var created = Try.of(() -> this.controller.insertIteration(iteration));
        
        // in case of success build and return response
        if(created.isSuccess()){
            return Mono.just(created.get());
        }
        
        return Mono.empty();
    }
    
    /**
     * Registers worker into the scheduler
     * 
     * @param input The worker input to register
     * @return Returns registered worker
     */
    @Override
    public Mono<Worker> registration(WorkerInput input){
        // do create worker
        var created = Try.of(() -> this.controller.insertWorker(input));
        
        // in case of success build and return response
        if(created.isSuccess()){
            return Mono.just(created.get());
        }
        
        return Mono.empty();
    }
    
    /**
     * Send a Heartbeat signal to from worker scheduler
     * 
     * @param id The identifier of worker instance
     * @param heartbeat The worker reported heartbeat
     * @return Returns updated agent definition
     */
    @Override
    public Mono<Worker> heartbeat(String id, WorkerHeartbeat heartbeat){
        // do update the worker
        var updated = Try.of(() -> this.controller.updateWorker(id, heartbeat));
        
        // in case of success build and return response
        if(updated.isSuccess()){
            return Mono.just(updated.get());
        }
        
        return Mono.empty();
    }
}
