package io.imast.work4j.controller;

import io.imast.work4j.data.SchedulerDataRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * The scheduler job controller module
 * 
 * @author davitp
 */
@Slf4j
public class SchedulerControllerBuilder {
    
    /**
     * The data repository for scheduler
     */
    protected SchedulerDataRepository data;
        
    /**
     * Creates new instance of Scheduler Job Controller Builder
     */
    protected SchedulerControllerBuilder(){
    }
    
    /**
     * Creates new builder instance to chain
     * 
     * @return Returns new builder
     */
    public static SchedulerControllerBuilder builder(){
        return new SchedulerControllerBuilder();
    }
    
    /**
     * Use the data repository
     * 
     * @param data The scheduler data repository
     * @return Returns builder instance for chaining
     */
    public SchedulerControllerBuilder withDataRepository(SchedulerDataRepository data){
        this.data = data;
        return this;
    }
    
    /**
     * Builds the final controller instance
     * 
     * @return Returns controller instance
     */
    public SchedulerController build(){
        return new SchedulerController(this.data);
    }
}
