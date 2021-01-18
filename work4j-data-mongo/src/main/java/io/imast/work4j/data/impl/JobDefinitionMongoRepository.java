package io.imast.work4j.data.impl;

import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.*;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import static com.mongodb.client.model.Sorts.*;
import io.imast.core.Coll;
import io.imast.core.Str;
import io.imast.core.Zdt;
import io.imast.core.mongo.BaseMongoRepository;
import io.imast.core.mongo.SimplePojoRegistries;
import io.imast.core.mongo.StringIdGenerator;
import io.imast.work4j.data.JobDefinitionRepository;
import io.imast.work4j.model.JobDefinition;
import io.imast.work4j.model.JobRequestResult;
import io.imast.work4j.model.JobStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.bson.conversions.Bson;
import java.util.stream.Collectors;
import org.bson.BsonDocument;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.ClassModel;

/**
 * The abstract repository for the job definition
 * 
 * @author davitp
 */
public class JobDefinitionMongoRepository extends BaseMongoRepository<String, JobDefinition> implements JobDefinitionRepository {
    
    /**
     * Creates new instance of job definition mongo repository
     * 
     * @param mongoDatabase The underlying mongo database
     */
    public JobDefinitionMongoRepository(MongoDatabase mongoDatabase){
        super(mongoDatabase, "work4j_job_definitions", JobDefinition.class);
    }
    
    /**
     * The custom Codec registry
     * 
     * @return Returns custom Codec registry
     */
    @Override
    protected CodecRegistry customizer(){
        return SimplePojoRegistries.simple(
            ClassModel.builder(this.clazz)
                    .idGenerator(new StringIdGenerator())
                    .build()
        );
    }
    
    /**
     * Get the page of job definitions sorted by code
     * 
     * @param page The page number
     * @param size The page size
     * @return Returns a page of job definitions
     */
    @Override
    public JobRequestResult getPageByCode(int page, int size){
        
        // paged result
        var part = this.getCollection().find().sort(descending("code")).skip(page * size).limit(size);   
        
        return new JobRequestResult(this.toList(part), this.getCollection().countDocuments());
    }
            
    /**
     * Find all jobs by type
     * 
     * @param type The type of job
     * @return Returns set of jobs
     */
    @Override
    public List<JobDefinition> getByType(String type){
        
        // get all filtered by by type field
        return this.toList(this.getCollection().find(eq("type", type)));
    }
    
    /**
     * Gets all entries by agent and status
     * 
     * @param type The type of job
     * @param group The group to filter
     * @param cluster The cluster
     * @param statuses The status
     * @return Returns filtered jobs
     */
    @Override
    public List<JobDefinition> getByStatusIn(String type, String group, String cluster, List<JobStatus> statuses){
        
        var filters = new ArrayList<Bson>();
        
        // add type filter if given
        if(!Str.blank(type)){
            filters.add(eq("type", type));
        }
        
        // add group filter if given
        if(!Str.blank(group)){
            filters.add(eq("group", group));
        }
        
        // add cluster filter if given
        if(!Str.blank(cluster)){
            filters.add(eq("cluster", cluster));
        }
        
        // add target statuses filter if any
        if(Coll.hasItems(statuses)){
            filters.add(in("status", statuses.stream().map(s -> s.toString()).collect(Collectors.toList())));
        }
        
        return this.toList(this.getCollection().find(filters.isEmpty() ? new BsonDocument() : and(filters)));
    }
    
    /**
     * Gets all entries by agent and status not in
     * 
     * @param type The type of job
     * @param group The group to filter
     * @param cluster The cluster
     * @param statuses The status
     * @return Returns filtered jobs
     */
    @Override
    public List<JobDefinition> getByStatusNotIn(String type, String group, String cluster, List<JobStatus> statuses){
        var filters = new ArrayList<Bson>();
        
        // add type filter if given
        if(!Str.blank(type)){
            filters.add(eq("type", type));
        }
        
        // add group filter if given
        if(!Str.blank(group)){
            filters.add(eq("group", group));
        }
        
        // add cluster filter if given
        if(!Str.blank(cluster)){
            filters.add(eq("cluster", cluster));
        }
        
        // add target statuses to exclude filter if any
        if(Coll.hasItems(statuses)){
            filters.add(not(in("status", statuses.stream().map(s -> s.toString()).collect(Collectors.toList()))));
        }
        
        return this.toList(this.getCollection().find(filters.isEmpty() ? new BsonDocument() : and(filters)));
    }
    
    /**
     * Gets all groups
     * 
     * @param cluster The target cluster
     * @return Returns all the groups in the system
     */
    @Override
    public List<String> getAllGroups(String cluster) {
        
        if(Str.blank(cluster)){
            return this.distinctScalar("group", String.class);
        }
        
        return this.getCollection().distinct("group", String.class).filter(eq("cluster", cluster)).into(new ArrayList<>());
    }

    /**
     * Gets all types available
     * 
     * @param cluster The target cluster
     * @return Returns all the types
     */
    @Override
    public List<String> getAllTypes(String cluster) {
        if(Str.blank(cluster)){
            return this.distinctScalar("type", String.class);
        }
        return this.getCollection().distinct("type", String.class).filter(eq("cluster", cluster)).into(new ArrayList<>());
    }
    
    /**
     * Update (insert if missing) job definition
     * 
     * @param jobDefinition The job definition to update
     * @return Returns userted job definition
     */
    @Override
    public Optional<JobDefinition> update(JobDefinition jobDefinition) {
        
        // try find existing job
        Optional<JobDefinition> existing = this.getById(jobDefinition.getId());
        
        // do not create if created
        if(!existing.isPresent()){
            return Optional.empty();
        }
        
        // building the definition
        var definitionBuilder = jobDefinition.toBuilder()
                .created(existing.get().getCreated())
                .createdBy(existing.get().getCreatedBy())
                .modified(Zdt.utc())
                .status(jobDefinition.getStatus() == null ? JobStatus.ACTIVE : jobDefinition.getStatus())
                .cluster(jobDefinition.getCluster()== null ? "DEFAULT_CLUSTER" : jobDefinition.getCluster());
        
        return super.upsert(definitionBuilder.build(), j -> j.getId());
    }
    
    /**
     * Inserts job definition into a collection
     * 
     * @param jobDefinition The job definition to insert
     * @return Returns saved entity if success
     */
    @Override
    public Optional<JobDefinition> insert(JobDefinition jobDefinition){
        
        // clone definition
        var definition = jobDefinition.toBuilder().build();
        
        // use code as ID to set
        definition.setId(definition.getCode());
        
        // try find existing job
        Optional<JobDefinition> existing = this.getById(definition.getId());
        
        // do not create if created
        if(existing.isPresent()){
            return Optional.empty();
        }
        
        // set creation and modification times
        definition.setCreated(Zdt.utc());
        definition.setModified(Zdt.utc());
        
        // set default status if missing
        definition.setStatus(definition.getStatus() == null ? JobStatus.ACTIVE : definition.getStatus());
        
        // assign to the default agent if not given
        definition.setCluster(definition.getCluster()== null ? "DEFAULT_CLUSTER" : definition.getCluster());
                
        return super.insert(definition);
    }
    
    /**
     * A special procedure to prepare schema
     * 
     * @return Returns if schema is ready
     */
    @Override
    public boolean prepare() {
        
        // create named index if does not exist
        var result = this.getCollection().createIndex(Indexes.ascending("code"), new IndexOptions().name("jobs_by_code"));
        
        // valid name returned
        return !Str.blank(result);
    }    
}
