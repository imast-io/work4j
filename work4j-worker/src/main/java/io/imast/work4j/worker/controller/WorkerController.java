package io.imast.work4j.worker.controller;

import io.imast.core.Lang;
import io.imast.core.Zdt;
import io.imast.work4j.channel.SchedulerChannel;
import io.imast.work4j.model.agent.AgentActivityType;
import io.imast.work4j.model.agent.AgentDefinition;
import io.imast.work4j.model.agent.AgentHealth;
import io.imast.work4j.worker.WorkerConfiguration;
import io.imast.work4j.worker.WorkerException;
import io.imast.work4j.worker.instance.QuartzInstance;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The worker controller  
 * 
 * @author davitp
 */
public class WorkerController {
   
    /**
     * The quartz instance
     */
    protected final QuartzInstance instance;
    
    /**
     * The scheduler channel
     */
    protected SchedulerChannel channel;

    /**
     * The listener
     */
    private final WorkerListener listener;
    
    /**
     * The worker configuration
     */
    protected final WorkerConfiguration config;
    
    /**
     * The asynchronous executor
     */
    protected final ScheduledExecutorService asyncExecutor;
    
    /**
     * The agent instance to register
     */
    protected AgentDefinition agent;
    
    /**
     * Creates new controller based on quartz instance and communication channel
     * 
     * @param instance The quartz instance
     * @param channel The channel
     * @param listener The worker listener
     * @param config The worker configuration
     */
    public WorkerController(QuartzInstance instance, SchedulerChannel channel, WorkerListener listener, WorkerConfiguration config){
        this.instance = instance;
        this.channel = channel;
        this.listener = listener;
        this.config = config;
        this.asyncExecutor =  Executors.newScheduledThreadPool(1);
    }
    
    /**
     * Initialize worker controller instance
     * 
     * @throws WorkerException 
     */
    public void initialize() throws WorkerException {
        
        // number of tries
        var agentTries = this.config.getAgentRegistrationTries();
        
        // by default try N times
        if(agentTries == null || agentTries <= 0){
            agentTries = 10;
        }
        
        // try to register agent
        this.agent = this.ensureRegister(agentTries);
        
        // make sure registered
        if(this.agent == null){
            throw new WorkerException("Unable to register agent definition");
        }
    }
    
    /**
     * Start worker
     */
    public void start(){
                
        // register listner function
        this.listener.add(this::recieved);
        
        // start listening
        this.listener.start();
        
        // if worker signal period is given 
        if(this.config.getWorkerSignalRate() != null && !this.config.getWorkerSignalRate().isZero()){
            this.asyncExecutor.scheduleAtFixedRate(() -> this.heartbeat(), 0, this.config.getWorkerSignalRate().toMillis(), TimeUnit.MILLISECONDS);
        }
    }
    
    /**
     * Stop worker
     */
    public void stop(){
        
        // stop listening
        this.listener.stop();
        
        // remove handler
        this.listener.remove(this::recieved); 
        
        // shutdown all async tasks
        this.asyncExecutor.shutdown();
    }
    
    /**
     * Process received update 
     * 
     * @param message The message to process 
     */
    protected void recieved(WorkerUpdateMessage message){
        
        // handle add operation
        if(message.getOperation() == UpdateOperation.ADD){
            this.instance.schedule(message.getDefinition());
        }
        
        // handle update operation
        if(message.getOperation() == UpdateOperation.UPDATE){
            this.instance.reschedule(message.getDefinition());
        }
        
        // handle remove operation
        if(message.getOperation() == UpdateOperation.REMOVE){
            this.instance.unschedule(message.getCode(), message.getGroup());
        }
    }
    
    /**
     * Ensures that agent client has been registered
     * 
     * @param tryCount Number of tries
     * @return Returns true if successful
     */
    protected AgentDefinition ensureRegister(int tryCount){
        
        // try several times
        for(int i = 0; i < tryCount; ++i){
            
            // register
            var agentResult = this.register();
            
            // if successfuly registered
            if(agentResult != null){
                return agentResult;
            }
            
            // delay for the next try
            Lang.wait(5000);
        }
        
        return null;
    }
    
    /**
     * Register itself to the scheduler
     * 
     * @return Returns created agent definition or null
     */
    protected AgentDefinition register(){
        
        // the agent signal rate
        var singalRate = this.config.getWorkerSignalRate();
        
        // now time
        var now = Zdt.utc();
        
        // worker at cluster identity
        var identity = String.format("%s@%s", this.instance.getWorker(), this.instance.getCluster());
        
        // the agent definition
        var entity = AgentDefinition.builder()
                .id(identity)
                .worker(this.instance.getWorker())
                .cluster(this.instance.getCluster())
                .name(identity)
                .supervisor(this.config.isSupervise())
                .health(new AgentHealth(now, AgentActivityType.REGISTER))
                .expectedSignalMinutes(singalRate.toSeconds() / 60.0)
                .registered(now)
                .build();
        
        return this.channel.registration(entity).orElse(null);
    }
    
    /**
     * Report the health to scheduler
     */
    private void heartbeat() {
                
        // report new health info
        this.channel.heartbeat(this.agent.getId(), new AgentHealth(Zdt.utc(), AgentActivityType.HEARTBEAT));
    }
}
