package io.imast.work4j.worker.controller;

import io.imast.work4j.channel.SchedulerChannel;
import io.imast.work4j.model.worker.Worker;
import io.imast.work4j.model.worker.WorkerActivity;
import io.imast.work4j.model.worker.WorkerHeartbeat;
import io.imast.work4j.worker.WorkerConfiguration;
import io.imast.work4j.worker.WorkerException;
import io.imast.work4j.worker.instance.QuartzInstance;
import java.util.List;
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
     * The supervisors
     */
    private final List<WorkerSupervior> supervisors;
    
    /**
     * The worker configuration
     */
    protected final WorkerConfiguration config;
    
    /**
     * The asynchronous executor
     */
    protected final ScheduledExecutorService asyncExecutor;
    
    /**
     * The registered worker instance 
     */
    protected final Worker worker;
    
    /**
     * Creates new controller based on quartz instance and communication channel
     * 
     * @param worker The worker instance
     * @param instance The quartz instance
     * @param channel The channel
     * @param supervisors The worker supervisors
     * @param config The worker configuration
     */
    public WorkerController(Worker worker, QuartzInstance instance, SchedulerChannel channel, List<WorkerSupervior> supervisors, WorkerConfiguration config){
        this.worker = worker;
        this.instance = instance;
        this.channel = channel;
        this.supervisors = supervisors;
        this.config = config;
        this.asyncExecutor = Executors.newScheduledThreadPool(1);
    }
        
    /**
     * Start worker
     * @throws io.imast.work4j.worker.WorkerException
     */
    public void start() throws WorkerException{
          
        this.instance.start();
        
        // subscribe to all supervisors
        this.supervisors.forEach(supervisor -> {
            // register listner function
            supervisor.add(this::recieved);

            // start listening
            supervisor.start();
        });
        
        // if worker signal period is not given do not send heartbeats
        if(this.config.getHeartbeatRate() == null || this.config.getHeartbeatRate() == 0){
            return;
        }
        
        // heartbeat with given frequency
        this.asyncExecutor.scheduleAtFixedRate(() -> this.heartbeat(), 0, this.config.getHeartbeatRate(), TimeUnit.MILLISECONDS);
    }
    
    /**
     * Stop worker
     * @throws io.imast.work4j.worker.WorkerException
     */
    public void stop() throws WorkerException{
        
        this.instance.stop();
        
        // subscribe to all supervisors
        this.supervisors.forEach(supervisor -> {
            // stop listening
            supervisor.stop();
            
            // remove listner function
            supervisor.remove(this::recieved);            
        });
        
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
     * Report the health to scheduler
     */
    protected void heartbeat() {
                
        // report new health info
        this.channel.heartbeat(this.worker.getId(), WorkerHeartbeat.builder().activity(WorkerActivity.HEARTBEAT).build());
    }
}
