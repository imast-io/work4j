package io.imast.work4j.controller;

import io.imast.work4j.channel.notify.WorkerPublisher;
import io.imast.work4j.data.AgentDefinitionRepository;
import io.imast.work4j.data.JobDefinitionRepository;
import io.imast.work4j.data.JobIterationRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * The scheduler job controller module
 * 
 * @author davitp
 */
@Slf4j
public class SchedulerControllerBuilder {

    /**
     * The set of worker publishers
     */
    protected final List<WorkerPublisher> workerPublishers;
    
    /**
     * The job definition repository
     */
    protected JobDefinitionRepository definitions;
    
    /**
     * The job iterations repository
     */
    protected JobIterationRepository iterations;
     
    /**
     * The agent definitions repository
     */
    protected AgentDefinitionRepository agents;
    
    /**
     * Creates new instance of Scheduler Job Controller Builder
     */
    protected SchedulerControllerBuilder(){
        this.workerPublishers = new ArrayList<>();
    }
    
    /**
     * Creates new builder instance to chain
     * 
     * @return Returns new builder
     */
    public static SchedulerControllerBuilder newBuilder(){
        return new SchedulerControllerBuilder();
    }
        
    /**
     * Use the given agent repository
     * 
     * @param agents The agent repository
     * @return Returns builder instance for chaining
     */
    public SchedulerControllerBuilder withAgents(AgentDefinitionRepository agents){
        this.agents = agents;
        return this;
    }
    
    /**
     * Use the given job repository
     * 
     * @param definition The job definitions repository
     * @return Returns builder instance for chaining
     */
    public SchedulerControllerBuilder withJobDefinitions(JobDefinitionRepository definition){
        this.definitions = definition;
        return this;
    }
    
    /**
     * Use the given job iterations repository
     * 
     * @param iterations The job iterations repository
     * @return Returns builder instance for chaining
     */
    public SchedulerControllerBuilder withJobIterations(JobIterationRepository iterations){
        this.iterations = iterations;
        return this;
    }
    
    /**
     * Use the given worker publisher
     * 
     * @param publisher The publisher to use
     * @return Returns builder instance for chaining
     */
    public SchedulerControllerBuilder withPublisher(WorkerPublisher publisher){
        this.workerPublishers.add(publisher);
        return this;
    }
    
    /**
     * Builds the final controller instance
     * 
     * @return Returns controller instance
     */
    public SchedulerController build(){
        return new SchedulerController(this.definitions, this.iterations, this.agents, this.workerPublishers).initialize();
    }
}
