package io.imast.samples.scheduler.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import io.imast.work4j.controller.SchedulerController;
import io.imast.work4j.controller.SchedulerControllerBuilder;
import io.imast.work4j.data.impl.SchedulerMongoRepisotory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * The main configuration beans
 * 
 * @author davitp
 */
@Configuration
@Slf4j
public class MainConfiguration {
    
    /**
     * The mongo URI
     */
    @Value("${imast.data.mongo.uri}")
    private String mongoUri;
    
    /**
     * The mongo database
     */
    @Value("${imast.data.mongo.db}")
    private String databaseName;
    
    /**
     * The mongo client for communication
     * 
     * @return Returns mongo client
     */
    @Lazy
    @Bean
    public MongoClient mongoClient(){
        return MongoClients.create(this.mongoUri);
    }
    
    /**
     * The mongo database for communication
     * 
     * @return Returns mongo database
     */
    @Lazy
    @Bean
    public MongoDatabase mongoDatabase(){
        return this.mongoClient().getDatabase(this.databaseName);
    }
    
    /**
     * The scheduler data repository
     * 
     * @return Returns data repository
     */
    @Lazy
    @Bean
    public SchedulerMongoRepisotory schedulerDataRepository(){
        
        // create new repository
        var repo = new SchedulerMongoRepisotory(this.mongoClient(), this.mongoDatabase(), false);
        
        // make sure schema is ready
        repo.ensureSchema();
        
        return repo;
    }
    
    /**
     * The scheduler controller bean
     * 
     * @return Returns scheduler controller bean
     */
    @Lazy
    @Bean
    public SchedulerController schedulerController(){
        return SchedulerControllerBuilder
                .builder()
                .withDataRepository(this.schedulerDataRepository())
                .build();
    }
}
