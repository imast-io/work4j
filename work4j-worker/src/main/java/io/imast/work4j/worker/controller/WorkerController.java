package io.imast.work4j.worker.controller;

import io.imast.work4j.channel.SchedulerChannel;
import io.imast.work4j.channel.worker.WorkerExecutionCompleted;
import io.imast.work4j.channel.worker.WorkerExecutionCreated;
import io.imast.work4j.channel.worker.WorkerExecutionPaused;
import io.imast.work4j.channel.worker.WorkerListener;
import io.imast.work4j.channel.worker.WorkerMessage;
import io.imast.work4j.model.worker.Worker;
import io.imast.work4j.model.worker.WorkerActivity;
import io.imast.work4j.model.worker.WorkerHeartbeat;
import io.imast.work4j.worker.WorkerConfiguration;
import io.imast.work4j.worker.WorkerException;
import io.imast.work4j.worker.instance.QuartzInstance;
import io.imast.work4j.worker.instance.ExecutionKey;
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
     * The listeners
     */
    private final List<WorkerListener> listeners;
    
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
     * @param listeners The worker listeners
     * @param config The worker configuration
     */
    public WorkerController(Worker worker, QuartzInstance instance, SchedulerChannel channel, List<WorkerListener> listeners, WorkerConfiguration config){
        this.worker = worker;
        this.instance = instance;
        this.channel = channel;
        this.listeners = listeners;
        this.config = config;
        this.asyncExecutor = Executors.newScheduledThreadPool(1);
    }
        
    /**
     * Start worker
     * @throws io.imast.work4j.worker.WorkerException
     */
    public void start() throws WorkerException{
          
        this.instance.start();
        
        // subscribe to all listeners
        this.listeners.forEach(listener -> {
            // register listner function
            listener.add(this::recieved);

            // start listening
            listener.start();
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
        
        // subscribe to all listeners
        this.listeners.forEach(listener -> {
            // stop listening
            listener.stop();
            
            // remove listner function
            listener.remove(this::recieved);            
        });
        
        // shutdown all async tasks
        this.asyncExecutor.shutdown();
    }
    
    /**
     * Process received update 
     * 
     * @param message The message to process 
     */
    protected void recieved(WorkerMessage message){
       
        // an execution is created so needs to be scheduled
        if(message instanceof WorkerExecutionCreated){
            
            // cast to concrete type
            var msg = (WorkerExecutionCreated) message;
            
            // schedule
            this.instance.schedule(msg.getExecution());
        }
        
        // an execution is completed so needs to be unscheduled
        if(message instanceof WorkerExecutionCompleted){
            
            // cast to concrete type
            var msg = (WorkerExecutionCompleted) message;
            
            // unschedule
            this.instance.unschedule(new ExecutionKey(msg.getExecutionId(), msg.getJobId()));
        }
        
        // an execution is paused so needs to be paused in scheduler instance
        if(message instanceof WorkerExecutionPaused){
            
            // cast to concrete type
            var msg = (WorkerExecutionPaused) message;
            
            // pause
            this.instance.pause(new ExecutionKey(msg.getExecutionId(), msg.getJobId()));
        }
       
        // an execution is resumed so needs to be resumed in scheduler instance
        if(message instanceof WorkerExecutionPaused){
            
            // cast to concrete type
            var msg = (WorkerExecutionPaused) message;
            
            // pause
            this.instance.resume(new ExecutionKey(msg.getExecutionId(), msg.getJobId()));
        }
    }
   
    /**
     * Report the health to scheduler
     */
    protected void heartbeat() {
                
        // report new health info
        this.channel.heartbeat(this.worker.getId(), WorkerHeartbeat.builder().activity(WorkerActivity.HEARTBEAT).build()).subscribe();
    }
}
