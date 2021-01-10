package io.imast.work4j.worker;

import java.util.List;
import java.util.Properties;
import io.imast.core.Str;
import io.imast.work4j.channel.SchedulerChannel;
import java.util.ArrayList;
import java.util.UUID;
import org.quartz.JobListener;
import org.quartz.SchedulerListener;
import org.quartz.TriggerListener;
import io.imast.work4j.worker.instance.*;

/**
 * A special builder to create quartz worker
 * 
 * @author davitp
 */
public class QuartzWorkerBuilder {
   
    /**
     * The instance to configuration
     */
    private final QuartzWorkerConfiguration config;
    
    /**
     * The worker factory instance
     */
    private final WorkerFactory factory;
    
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
     * The cluster name
     */
    private final String cluster;
    
    /**
     * The worker name
     */
    private final String worker;

    /**
     * The scheduler channel
     */
    private SchedulerChannel schedulerChannel;
    
    /**
     * Creates new instance of builder from configuration
     * 
     * @param config The configuration instance
     */
    private QuartzWorkerBuilder(QuartzWorkerConfiguration config){
        this.config = config;
        this.schedulerListeners = new ArrayList<>();
        this.jobListeners = new ArrayList<>();
        this.triggerListeners = new ArrayList<>();
        this.cluster = Str.blank(this.config.getCluster()) ? JobConstants.DEFAULT_CLUSTER : this.config.getCluster();
        this.worker = Str.blank(this.config.getWorker()) ? UUID.randomUUID().toString() : this.config.getWorker();
        this.factory = new WorkerFactory();
    }
    
    /**
     * Creates new instance of builder from configuration
     * 
     * @param config The configuration instance
     * @return Returns an instance to builder for chaining
     */
    public static QuartzWorkerBuilder builder(QuartzWorkerConfiguration config){
        return new QuartzWorkerBuilder(config);
    }
    
    /**
     * Sets a scheduler channel for worker/controller communication
     * 
     * @param channel The channel instance
     * @return Returns builder for chaining
     */
    public QuartzWorkerBuilder channel(SchedulerChannel channel){
        this.schedulerChannel = channel;
        return this;
    }
    
    /**
     * Adds a job type and class association
     * 
     * @param type The job type code
     * @param clazz The job class instance
     * @return Returns builder for chaining
     */
    public QuartzWorkerBuilder withJob(String type, Class clazz){
        this.factory.registerJobClass(type, clazz);
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
    public QuartzWorkerBuilder withModule(String type, String key, Object module){
        this.factory.registerModule(type, key, module);
        return this;
    }
    
    /**
     * Use the given listener
     * 
     * @param listener The listener to attach
     * @return Returns builder for chaining
     */
    public QuartzWorkerBuilder withListener(SchedulerListener listener){
        this.schedulerListeners.add(listener);
        return this;
    }
    
    /**
     * Use the given listener
     * 
     * @param listener The listener to attach
     * @return Returns builder for chaining
     */
    public QuartzWorkerBuilder withListener(JobListener listener){
        this.jobListeners.add(listener);
        return this;
    }
    
    /**
     * Use the given listener
     * 
     * @param listener The listener to attach
     * @return Returns builder for chaining
     */
    public QuartzWorkerBuilder withListener(TriggerListener listener){
        this.triggerListeners.add(listener);
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
        
        // set instance name for quartz scheduler
        props.setProperty("org.quartz.scheduler.instanceName", "WORK4J_" + this.cluster);
        props.setProperty("org.quartz.scheduler.instanceId", "WORK4J_" + this.worker);
        props.setProperty("org.quartz.threadPool.threadCount", this.config.getParallelism().toString());
        
        // other props
        props.setProperty("org.quartz.scheduler.skipUpdateCheck", "true");
        props.setProperty("org.quartz.scheduler.jobFactory.class", "org.quartz.simpl.SimpleJobFactory");

        // if JDBC clustering
        if(this.config.getClusteringType() == ClusteringType.JDBC){
            props.setProperty("org.quartz.jobStore.class", "org.quartz.impl.jdbcjobstore.JobStoreTX");
            props.setProperty("org.quartz.jobStore.driverDelegateClass", "org.quartz.impl.jdbcjobstore.StdJDBCDelegate");
            props.setProperty("org.quartz.jobStore.dataSource", this.config.getDataSource());
            props.setProperty("org.quartz.jobStore.tablePrefix", "QRTZ_");

            // prefix for data store property
            var dsPropPrefix = String.format("org.quartz.dataSource.%s", this.config.getDataSource());
            
            // data store properties
            props.setProperty(String.format("%s.%s", dsPropPrefix, "driver"), "com.mysql.jdbc.Driver");
            props.setProperty(String.format("%s.%s", dsPropPrefix, "URL"), this.config.getDataSourceUri());
            props.setProperty(String.format("%s.%s", dsPropPrefix, "user"), this.config.getDataSourceUsername());
            props.setProperty(String.format("%s.%s", dsPropPrefix, "password"), this.config.getDataSourcePassword());
            props.setProperty(String.format("%s.%s", dsPropPrefix, "maxConnections"), "30");
        }
        
        return props;
    }
    
}
