package io.imast.work4j.worker.controller;

import java.util.List;
import java.util.Properties;
import io.imast.work4j.channel.SchedulerChannel;
import io.imast.work4j.channel.worker.WorkerListener;
import io.imast.work4j.execution.JobExecutor;
import io.imast.work4j.execution.JobExecutorContext;
import io.imast.work4j.model.cluster.ClusterWorker;
import io.imast.work4j.model.cluster.WorkerKind;
import io.imast.work4j.worker.ClusteringType;
import io.imast.work4j.worker.JobConstants;
import io.imast.work4j.worker.PersistenceType;
import io.imast.work4j.worker.WorkerConfiguration;
import io.imast.work4j.worker.WorkerException;
import io.imast.work4j.worker.WorkerFactory;
import io.imast.work4j.worker.instance.EveryJobListener;
import io.imast.work4j.worker.instance.EveryTriggerListener;
import io.imast.work4j.worker.instance.JobSchedulerListener;
import io.imast.work4j.worker.instance.QuartzInstance;
import io.vavr.control.Try;
import java.util.ArrayList;
import java.util.HashMap;
import org.quartz.JobListener;
import org.quartz.SchedulerListener;
import org.quartz.TriggerListener;
import java.util.Map;
import java.util.function.Function;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

/**
 * A special builder to create quartz worker
 * 
 * @author davitp
 */
public class WorkerControllerBuilder {
   
    /**
     * The instance to configuration
     */
    private final WorkerConfiguration config;
    
    /**
     * The worker factory instance
     */
    private final WorkerFactory factory;
    
     /**
     * The job modules by type
     */
    protected final Map<String, Map<String, Object>> jobModules;
    
    /**
     * The set of scheduler listeners
     */
    private final List<SchedulerListener> schedulerListeners;
    
    /**
     * The set of job listeners
     */
    private final List<JobListener> jobListeners;
    
    /**
     * The set of trigger listeners
     */
    private final List<TriggerListener> triggerListeners;
    
    /**
     * The set of worker listeners
     */
    private final List<WorkerListener> listeners;
    
    /**
     * The worker instance
     */
    private ClusterWorker worker;
    
    /**
     * The scheduler channel
     */
    private SchedulerChannel schedulerChannel;
    
    /**
     * Creates new instance of builder from configuration
     * 
     * @param config The configuration instance
     */
    private WorkerControllerBuilder(WorkerConfiguration config){
        this.config = config;
        this.schedulerListeners = new ArrayList<>();
        this.jobListeners = new ArrayList<>();
        this.triggerListeners = new ArrayList<>();
        this.listeners = new ArrayList<>();
        this.factory = new WorkerFactory();
        this.jobModules = new HashMap<>();
    }
    
    /**
     * Creates new instance of builder from configuration
     * 
     * @param config The configuration instance
     * @return Returns an instance to builder for chaining
     */
    public static WorkerControllerBuilder builder(WorkerConfiguration config){
        return new WorkerControllerBuilder(config);
    }
    
    /**
     * Sets a scheduler channel for worker/controller communication
     * 
     * @param channel The channel instance
     * @return Returns builder for chaining
     */
    public WorkerControllerBuilder withChannel(SchedulerChannel channel){
        this.schedulerChannel = channel;
        return this;
    }
    
    /**
     * Sets a worker instance for the controller
     * 
     * @param worker The worker instance
     * @return Returns builder for chaining
     */
    public WorkerControllerBuilder withWorker(ClusterWorker worker){
        this.worker = worker;
        return this;
    }
    
    /**
     * Adds a job type and class association
     * 
     * @param type The job type code
     * @param executorSupplier The job class instance
     * @return Returns builder for chaining
     */
    public WorkerControllerBuilder withJobExecutor(String type, Function<JobExecutorContext, JobExecutor> executorSupplier){
        this.factory.registerExecutor(type, executorSupplier);
        return this;
    }
    
    /**
     * Adds instances for required job modules
     * 
     * @param type The job type code
     * @param key The key of module
     * @param module The module instance or supplier
     * @return Returns builder for chaining
     */
    public WorkerControllerBuilder withModule(String type, String key, Object module){
        this.registerModule(type, key, module);
        return this;
    }
    
    /**
     * Use the given listener
     * 
     * @param listener The listener to add
     * @return Returns builder for chaining
     */
    public WorkerControllerBuilder withListener(WorkerListener listener){
        this.listeners.add(listener);
        return this;
    }
    
    /**
     * Initialize quartz properties
     * 
     * @return Returns quartz props
     */
    private Properties quartzProps() {
        
        // props
        var props = new Properties();
        
        // the worker is balanced
        var clustered = this.worker.getKind() == WorkerKind.BALANCED;
        
        // use worker name as instance name for scheduler
        var instanceName = clustered ? this.worker.getCluster() : this.worker.getName();
        
        // use worker name as id, it's unique in case of clustered mode
        var instanceId = this.worker.getName();
        
        // set instance name for quartz scheduler
        props.setProperty("org.quartz.scheduler.instanceName", "WORK4J_" + instanceName);
        props.setProperty("org.quartz.scheduler.instanceId", "WORK4J_" + instanceId);
        props.setProperty("org.quartz.threadPool.threadCount", this.config.getParallelism().toString());
        
        // other props
        props.setProperty("org.quartz.scheduler.skipUpdateCheck", "true");
        props.setProperty("org.quartz.scheduler.jobFactory.class", "org.quartz.simpl.SimpleJobFactory");

        // mark as clustered if needed
        if(clustered){
            props.setProperty("org.quartz.jobStore.isClustered", "true");
            props.setProperty("org.quartz.scheduler.clusterCheckinInterval ", Long.toString(this.worker.getMaxIdle()));
        }
        
        // persistance enabled
        var persist = this.config.getPersistenceType() != PersistenceType.NO;
        
        // should set persistance-related attributes
        if(persist){
            // prefix for data store property
            var dsPropPrefix = String.format("org.quartz.dataSource.%s", this.config.getDataSource());
            
            // data store properties
            props.setProperty(String.format("%s.%s", dsPropPrefix, "driver"), "com.mysql.jdbc.Driver");
            props.setProperty(String.format("%s.%s", dsPropPrefix, "URL"), this.config.getDataSourceUri());
            props.setProperty(String.format("%s.%s", dsPropPrefix, "user"), this.config.getDataSourceUsername());
            props.setProperty(String.format("%s.%s", dsPropPrefix, "password"), this.config.getDataSourcePassword());
            props.setProperty(String.format("%s.%s", dsPropPrefix, "maxConnections"), "30");
            
            // set store's data source
            props.setProperty("org.quartz.jobStore.dataSource", this.config.getDataSource());
        }
        
        // if JDBC clustering
        if(this.config.getClusteringType() == ClusteringType.JDBC){
            props.setProperty("org.quartz.jobStore.tablePrefix", "QRTZ_");
            props.setProperty("org.quartz.jobStore.class", "org.quartz.impl.jdbcjobstore.JobStoreTX");
            props.setProperty("org.quartz.jobStore.driverDelegateClass", "org.quartz.impl.jdbcjobstore.StdJDBCDelegate");            
        }
        
        return props;
    }
    
    /**
     * Register a module for the given job type with the given key
     * 
     * @param type The job type
     * @param key The module key
     * @param module The module instance
     */
    private void registerModule(String type, String key, Object module){
        
        // add type if missing
        if(!this.jobModules.containsKey(type)){
            this.jobModules.put(type, new HashMap<>());
        }
        
        // register module
        this.jobModules.get(type).put(key, module);
    }
    
    /**
     * Initialize an instance of quartz scheduler 
     * 
     * @return Returns ready-to-use quartz scheduler instance
     * @throws WorkerException 
     */
    private Scheduler initScheduler() throws WorkerException{
     
        // properties for the quartz scheduler
        var props = this.quartzProps();
                
        // assign scheduler factory
        var schedulerFactory = Try.of(() -> new StdSchedulerFactory(props));
        
        // could not create scheduler factory
        if(schedulerFactory.isFailure()){
            throw new WorkerException("Could not create quartz scheduler factory", schedulerFactory.getCause());
        }
        
        // try get scheduler from factory
        var tryScheduler = Try.of(() -> schedulerFactory.get().getScheduler());
        
        // validity indicator
        if(tryScheduler.isFailure()){
            throw new WorkerException("Could not initialize quartz scheduler instance", tryScheduler.getCause());
        }
        
        // the scheduler object
        var scheduler = tryScheduler.get();

        // initialize context modules of scheduler
        try {
            scheduler.getContext().put(JobConstants.WORKER_FACTORY, this.factory);
            scheduler.getContext().put(JobConstants.JOB_MODULES, this.jobModules);
        }
        catch(SchedulerException ex){
            throw new WorkerException("Could not add context modules to scheduler", ex);
        }
        
        // add work4j listeners
        try{
            scheduler.getListenerManager().addSchedulerListener(new JobSchedulerListener(scheduler, this.schedulerChannel));
            scheduler.getListenerManager().addJobListener(new EveryJobListener(this.worker, this.schedulerChannel));
            scheduler.getListenerManager().addTriggerListener(new EveryTriggerListener(this.schedulerChannel));
        }
        catch(SchedulerException ex){
            throw new WorkerException("Could not register Work4j listeners to quartz scheduler", ex);
        }
        
        // add custom scheduler
        try{
            for(var listener : this.schedulerListeners){
                scheduler.getListenerManager().addSchedulerListener(listener);
            }
            for(var listener : this.jobListeners){
                scheduler.getListenerManager().addJobListener(listener);
            }
            for(var listener : this.triggerListeners){
                scheduler.getListenerManager().addTriggerListener(listener);
            }
        }
        catch(SchedulerException ex){
            throw new WorkerException("Could not register custom listeners to quartz scheduler", ex);
        }
                
        return scheduler;
    }
    
    /**
     * Build a ready-to-use worker controller
     * 
     * @return Returns worker controller
     * @throws io.imast.work4j.worker.WorkerException
     */
    public WorkerController build() throws WorkerException {
        
        // validate factory
        if(this.factory == null){
            throw new WorkerException("Worker Factory is required");
        }
        
        // validate worker
        if(this.worker == null){
            throw new WorkerException("Worker Instance is required");
        }
        
        // create a scheduler instance
        var scheduler = this.initScheduler();
     
        // list of all listeners
        var allListeners = new ArrayList<WorkerListener>();
        
        // create a quartz instance
        var instance = new QuartzInstance(scheduler);
        
        // if polling rate is specified create a supervisor
        if(this.config.getPollingRate() != null && this.config.getPollingRate() > 0){
            allListeners.add(new PollingWorkerListener(this.worker, instance, this.schedulerChannel, this.config));
        }
        
        // add rest of supervisors
        allListeners.addAll(this.listeners);
        
        // in case of standalone scheduler at least one listener should be there
        if(this.config.getClusteringType() == ClusteringType.STANDALONE){
            throw new WorkerException("Standalone scheduling needs at least one listener or a positive polling rate");
        }
        
        return new WorkerController(this.worker, instance, this.schedulerChannel, allListeners, this.config);
    }
}
