package io.imast.samples.scheduler.worker;

import io.imast.core.Lang;
import io.imast.work4j.worker.ClusteringType;
import io.imast.work4j.worker.PersistenceType;
import io.imast.work4j.worker.WorkerConfiguration;
import io.imast.work4j.worker.WorkerConnector;
import io.imast.work4j.worker.WorkerException;
import io.imast.work4j.worker.controller.WorkerControllerBuilder;
import io.vavr.control.Try;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
/**
 * The client test app
 * 
 * @author davitp
 */
@Slf4j
public class WorkerApplication {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args){
        
        var local = false;
        
        // indicate polling rate for worker        
        Long pollRate = Duration.ofSeconds(30L).toMillis();
        
        String env = null;
        String mysqlhost = "work4j_sample_mysqlcluster";
        
        if(local){
            env = "localhost";
            mysqlhost = "localhost";
        }
        
        // get the cluster name
        var cluster = System.getenv("IMAST_WORKER_CLUSTER_NAME");
        
        // the worker name
        var worker = System.getenv("IMAST_WORKER_WORKER_NAME");
                
        var config = WorkerConfiguration.builder()
                .cluster(cluster)
                .name(worker)
                .clusteringType(ClusteringType.BALANCED)
                .persistenceType(PersistenceType.MYSQL)
                .dataSourceUri(String.format("jdbc:mysql://%s:8810/quartz_scheduler", mysqlhost))
                .dataSource("jdbcds")
                .dataSourceUsername("username")
                .dataSourcePassword("password")
                .pollingRate(pollRate)
                .parallelism(8L)
                .workerRegistrationTries(10)
                .heartbeatRate(Duration.ofSeconds(15).toMillis())
                .build();
        
        // the localhost discovery client (use null in docker environment)
        var discovery = new SimpleDiscoveryClient(env);
        
        // worker channel implementation instance
        var channel = new WorkerChannelImpl(discovery);
        
        // connect and get worker
        var clusterWorker = new WorkerConnector(config, channel).connect();
        
        log.info(String.format("Connected to cluster (%s) with as worker %s in %s mode", clusterWorker.getCluster(), clusterWorker.getName(), clusterWorker.getKind()));
        
        var workerController = Try.of(() -> WorkerControllerBuilder
                .builder(config)
                .withChannel(channel)
                .withWorker(clusterWorker)
                .withJobExecutor("ECHO_JOB", context -> new EchoJob(context))
                .withModule("ECHO_JOB", "PRINTER", new PrinterModule())
                .build()).getOrNull();
        
        try {
        
            workerController.start();
            
        } catch (WorkerException ex) {
            throw new RuntimeException(ex);
        }
        
        Lang.wait((int)Duration.ofHours(1).toMillis());
    }
}
