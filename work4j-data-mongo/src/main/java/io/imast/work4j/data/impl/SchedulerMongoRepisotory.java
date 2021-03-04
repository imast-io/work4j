package io.imast.work4j.data.impl;

import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;
import static com.mongodb.client.model.Filters.lt;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Sorts.descending;
import io.imast.core.Lang;
import io.imast.core.Str;
import io.imast.work4j.data.exception.SchedulerDataException;
import io.imast.work4j.model.JobDefinition;
import io.imast.work4j.model.JobDefinitionInput;
import io.imast.work4j.model.JobRequestResult;
import io.imast.work4j.model.TriggerType;
import io.imast.work4j.model.execution.ExecutionStatus;
import io.imast.work4j.model.execution.ExecutionUpdateInput;
import io.imast.work4j.model.execution.ExecutionsResponse;
import io.imast.work4j.model.execution.JobExecution;
import io.imast.work4j.model.execution.JobExecutionInput;
import io.imast.work4j.model.iterate.Iteration;
import io.imast.work4j.model.iterate.IterationInput;
import io.imast.work4j.model.iterate.IterationStatus;
import io.imast.work4j.model.iterate.IterationsResponse;
import io.imast.work4j.model.cluster.WorkerActivity;import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import io.imast.work4j.data.SchedulerDataRepository;
import io.imast.work4j.model.Jobs;
import io.imast.work4j.model.cluster.ClusterDefinition;
import io.imast.work4j.model.cluster.WorkerJoinInput;
import io.imast.work4j.model.cluster.ClusterWorker;
import io.imast.work4j.model.cluster.Clusters;
import io.imast.work4j.model.cluster.WorkerHeartbeat;
import io.imast.work4j.model.cluster.WorkerKind;
import io.imast.work4j.model.execution.ExecutionIndexEntry;
import java.util.HashMap;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/**
 * The mongo repository for the scheduler data
 * 
 * @author davitp
 */
@Slf4j
public class SchedulerMongoRepisotory implements SchedulerDataRepository {

    /**
     * The table prefix for the source collections
     */
    protected static final String COLLECTION_PREFIX = "work4j";
        
    /**
     * The mongo database client
     */
    protected final MongoClient client;
    
    /**
     * The mongo database instance
     */
    protected final MongoDatabase mongoDatabase;
    
    /**
     * The job definitions collection
     */
    protected final MongoCollection<JobDefinition> definitions;
    
    /**
     * The iterations collection
     */
    private final MongoCollection<Iteration> iterations;
    
    /**
     * The clusters collection
     */
    private final MongoCollection<ClusterDefinition> clusters;
    
    /**
     * The executions collection
     */
    private final MongoCollection<JobExecution> executions;
    
    /**
     * Indicates if operations should be transactional
     */
    private final boolean transactional;
    
    /**
     * Creates new instance of scheduler mongo repository
     * 
     * @param client The client to mongo
     * @param mongoDatabase The mongo database reference
     * @param transactional Should operations be performed in transaction
     */
    public SchedulerMongoRepisotory(MongoClient client, MongoDatabase mongoDatabase, boolean transactional){
        this.client = client;
        this.mongoDatabase = mongoDatabase;
        this.definitions = MongoOps.withPojo(this.mongoDatabase.getCollection(this.collection("definitions"), JobDefinition.class));
        this.iterations = MongoOps.withPojo(this.mongoDatabase.getCollection(this.collection("iterations"), Iteration.class));
        this.clusters = MongoOps.withPojo(this.mongoDatabase.getCollection(this.collection("clusters"), ClusterDefinition.class));
        this.executions = MongoOps.withPojo(this.mongoDatabase.getCollection(this.collection("executions"), JobExecution.class));
        this.transactional = transactional;
    }
    
    /**
     * Ensures that schema is ready for data operations
     * 
     * @throws SchedulerDataException 
     */
    @Override
    public void ensureSchema() throws SchedulerDataException {
        
        try {
            
            // index job definitions by name (ascending)
            this.definitions.createIndex(Indexes.ascending("name"), new IndexOptions().name("jobs_by_name"));
            
            // index jobs by update time (descending)
            this.definitions.createIndex(Indexes.descending("modified"), new IndexOptions().name("jobs_by_modified"));
            
            // create unique index for (name, folder) pair
            this.definitions.createIndex(Indexes.ascending("name", "folder"), new IndexOptions().name("job_unique_name_folder").unique(true));
            
            // index executions by name
            this.executions.createIndex(Indexes.ascending("name"), new IndexOptions().name("executions_by_name"));
            
            // index executions by job id
            this.executions.createIndex(Indexes.ascending("jobId"), new IndexOptions().name("executions_by_jobId"));
            
            // index executions by update time
            this.executions.createIndex(Indexes.descending("modified"), new IndexOptions().name("executions_by_modified"));
            
            // index iterations by timestamp for easy paging
            this.iterations.createIndex(Indexes.descending("timestamp"), new IndexOptions().name("iteration_by_timestamp_desc"));
            
            // index iterations by execution id for easy filtering
            this.iterations.createIndex(Indexes.ascending("executionId"), new IndexOptions().name("iteration_by_exec_id_desc"));
            
            // index clusters by "cluster" value to ensure uniqueness
            this.clusters.createIndex(Indexes.ascending("cluster"), new IndexOptions().name("clusters_by_cluster_unique").unique(true));

        }
        catch (Throwable e){
            throw new SchedulerDataException("Indexing Error", Arrays.asList("Could not create schema indexes"), e);
        }
    }
    
    /**
     * Gets all the job definitions
     * 
     * @param cluster The cluster to filter
     * @param type The type of jobs
     * @return Returns set of all job definitions
     * @throws SchedulerDataException
     */
    @Override
    public List<JobDefinition> getAllJobs(String cluster, String type) throws SchedulerDataException {
        
        // the target filters
        var filters = new ArrayList<Bson>();
        
        // add type filter if given
        if(!Str.blank(type)){
            filters.add(eq("type", type));
        }
               
        // add cluster filter if given
        if(!Str.blank(cluster)){
            filters.add(eq("cluster", cluster));
        }
        
        // combined filter
        var combined = filters.isEmpty() ? new BsonDocument() : and(filters);

        // find all elements with filter
        return this.handle(() -> MongoOps.withinSession(this.transactional, this.client, session -> {
            return this.definitions.find(session, combined).into(new ArrayList<>());
        }));
    }
    
    /**
     * Gets the job definition by identifier
     * 
     * @param id The job definition id
     * @return Returns job definition if found
     * @throws SchedulerDataException
     */
    @Override
    public Optional<JobDefinition> getJobById(String id) throws SchedulerDataException {
        
        // id is required
        if(Str.blank(id)){
            throw new SchedulerDataException("Missing Id", Arrays.asList("Job ID is required"));
        }
        
        return this.handle(() -> MongoOps.withinSession(this.transactional, this.client, session -> {
            return Optional.ofNullable(this.definitions.find(session, this.hasId(id)).first());
        }));   
    }
    
    /**
     * Get the page of job definitions sorted by code
     * 
     * @param cluster The cluster to filter
     * @param type The type of jobs
     * @param page The page number
     * @param size The page size
     * @return Returns a page of job definitions
     * @throws SchedulerDataException
     */
    @Override
    public JobRequestResult getJobPage(String cluster, String type, int page, int size) throws SchedulerDataException {
    
        // the target filters
        var filters = new ArrayList<Bson>();
        
        // add type filter if given
        if(!Str.blank(type)){
            filters.add(eq("type", type));
        }
              
        // add cluster filter if given
        if(!Str.blank(cluster)){
            filters.add(eq("cluster", cluster));
        }
        
        // combined filter
        var combined = filters.isEmpty() ? new BsonDocument() : and(filters);
        
        // find all elements with filter
        return this.handle(() -> MongoOps.withinSession(this.transactional, this.client, session -> {
            
            // get filtered page
            var filtered = this.definitions
                    .find(session, combined)
                    .sort(descending("name"))
                    .skip(page * size)
                    .limit(size)
                    .into(new ArrayList<>());
            
            // count overall documents in query
            var count = this.definitions.countDocuments(session, combined);
            
            return new JobRequestResult(filtered, count);
        }));
    }
    
    /**
     * Saves a job definition into the data store
     * 
     * @param input The job definition input to save
     * @param replace The optional flag that allows to replace
     * @return Returns saved job definition
     * @throws SchedulerDataException
     */
    @Override
    public JobDefinition insertJob(JobDefinitionInput input, boolean replace) throws SchedulerDataException {
        
        // the input validation
        var validation = this.validateDefinitionInput(input);
        
        // in case of any errors do not continue
        if(!validation.isEmpty()){
            throw new SchedulerDataException("Invalid Definition", validation);
        }
        
        // the existing item filter
        var existingFilter = and(
                eq("name", input.getName()),
                eq("folder", input.getFolder())
        );
        
        // do within transaction 
        return this.handle(() -> MongoOps.withinSession(this.transactional, this.client, session -> {
            
            // try get existing
            var existing = this.definitions.find(session, existingFilter).first();
            
            // if already exists and no replacement is required report an error
            if(existing != null && !replace){
                throw new SchedulerDataException("Duplicate Definition", Arrays.asList("The item with given location already exists"));
            }
            
            // if item exists but replacement is allowed perform an update operation insted
            if(existing != null && replace){
                return this.updateJobImpl(session, existing, input);
            }
            
            // get current time
            var now = new Date();
            
            // generate new id for object
            var newId = ObjectId.get().toHexString();
            
            // construct new definition to save
            var definition = JobDefinition.builder()
                    .id(newId)
                    .name(input.getName())
                    .folder(input.getFolder())
                    .type(input.getType())
                    .cluster(input.getCluster())
                    .triggers(input.getTriggers())
                    .options(input.getOptions())
                    .selectors(input.getSelectors())
                    .payload(input.getPayload())
                    .extra(input.getExtra())
                    .createdBy(input.getCreatedBy())
                    .modifiedBy(input.getCreatedBy())
                    .created(now)
                    .modified(now)
                    .build();
            
            // perform insert operation
            var inserted = this.definitions.insertOne(session, definition);
            
            // could not insert
            if(inserted.getInsertedId() == null){
                throw new SchedulerDataException("Definition Not Saved", Arrays.asList("The definition was not saved"));
            }
            
            // get saved object if available
            var savedOne = this.definitions.find(session, this.hasId(newId)).first();
            
            // something went wrong and saved entity is missing
            if(savedOne == null){
                throw new SchedulerDataException("Definition Missing", Arrays.asList("The definition was not saved"));
            }
            
            // return new entity
            return savedOne;
        }));
    }
    
    /**
     * Updates an existing job definition 
     * 
     * @param id The job definition id
     * @param input The job definition input to update
     * @return Returns saved job definition
     * @throws SchedulerDataException
     */
    @Override
    public JobDefinition updateJob(String id, JobDefinitionInput input) throws SchedulerDataException {
        
        // make sure id is provided for update
        if(Str.blank(id)){
            throw new SchedulerDataException("Update Failed", Arrays.asList("The job update requires a valid identifier"));
        }
        
        // the input validation
        var validation = this.validateDefinitionInput(input);
        
        // in case of any errors do not continue
        if(!validation.isEmpty()){
            throw new SchedulerDataException("Invalid Definition", validation);
        }
        
        // do within transaction 
        return this.handle(() -> MongoOps.withinSession(this.transactional, this.client, session -> {
            
            // get existing instance to update
            var existing = this.definitions.find(session, this.hasId(id)).first();
            
            // there is no existing object to update
            if(existing == null){
                throw new SchedulerDataException("Update Error", Arrays.asList("The entity with given id is missing."));
            }
            
            return this.updateJobImpl(session, existing, input);
        }));
    }
    
    /**
     * Deletes an entry by id and returns deleted one
     * 
     * @param id The id of job definition to delete
     * @return Returns deleted job definition item
     * @throws SchedulerDataException
     */
    @Override
    public Optional<JobDefinition> deleteJobById(String id) throws SchedulerDataException {
        
        // id is required
        if(Str.blank(id)){
            throw new SchedulerDataException("Missing Id", Arrays.asList("Job ID is required"));
        }
        
        // do within transaction 
        return this.handle(() -> MongoOps.withinSession(this.transactional, this.client, session -> {
            
            // get existing item by id
            var existing = this.definitions.find(session, this.hasId(id)).first();
            
            // delete if exists
            if(existing != null) {
                // delete single entity
                this.definitions.deleteOne(session, this.hasId(id));
            }
            
            return Optional.ofNullable(existing);
        }));
    }
    
    /**
     * Deletes an entry by location
     * 
     * @param folder The folder of target job
     * @param name The name of job in folder
     * @return Returns deleted job definition item
     * @throws SchedulerDataException
     */
    @Override
    public Optional<JobDefinition> deleteJobByPath(String folder, String name) throws SchedulerDataException {
        
        // name and folder is required
        if(Str.blank(name) || Str.blank(folder)){
            throw new SchedulerDataException("Missing Name or Folder", Arrays.asList("Job Name and Folder are required"));
        }
        
        // the existing item filter
        var existingFilter = and(
                eq("name", name),
                eq("folder", folder)
        );
        
        // do within transaction 
        return this.handle(() -> MongoOps.withinSession(this.transactional, this.client, session -> {
            
            // get existing item by name and folder
            var existing = this.definitions.find(session, existingFilter).first();
            
            // delete if exists
            if(existing != null) {
                // delete single entity
                this.definitions.deleteOne(session, this.hasId(existing.getId()));
            }
            
            return Optional.ofNullable(existing);
        }));
    }
    
    /**
     * Deletes all the records
     * 
     * @return Returns number of deleted records
     * @throws SchedulerDataException
     */
    @Override
    public long deleteAllJobs() throws SchedulerDataException {
        
        // do within transaction 
        return this.handle(() -> MongoOps.withinSession(this.transactional, this.client, session -> {
            return this.definitions.deleteMany(session, new BsonDocument()).getDeletedCount();
        }));
    }
    
    /**
     * Gets all the job executions
     * 
     * @param cluster The optional target cluster to filter
     * @param type The optional type to filter by 
     * @return Returns set of all job executions
     * @throws SchedulerDataException
     */
    @Override
    public List<JobExecution> getAllExecutions(String cluster, String type) throws SchedulerDataException {
    
        // the target filters
        var filters = new ArrayList<Bson>();
        
        // add type filter if given
        if(!Str.blank(type)){
            filters.add(eq("type", type));
        }
        
        // add cluster filter if given
        if(!Str.blank(cluster)){
            filters.add(eq("cluster", cluster));
        }
        
        // combined filter
        var combined = filters.isEmpty() ? new BsonDocument() : and(filters);
        
        // find all elements with filter
        return this.handle(() -> MongoOps.withinSession(this.transactional, this.client, session -> {
            return this.executions.find(session, combined).into(new ArrayList<>());
        }));
    }
    
    /**
     * Gets all the job executions of job
     * 
     * @param jobId The job id to filter
     * @return Returns set of all job executions
     * @throws SchedulerDataException
     */
    @Override
    public List<JobExecution> getExecutionsByJob(String jobId) throws SchedulerDataException {
        
        // check if job id is not given
        if(Str.blank(jobId)){
            throw new SchedulerDataException("Job ID is missing", Arrays.asList("Job ID should be set to get its executions"));
        }
        
        // find all elements with filter
        return this.handle(() -> MongoOps.withinSession(this.transactional, this.client, session -> {
            return this.executions.find(session, eq("jobId", jobId)).into(new ArrayList<>());
        }));
    }
    
    /**
     * Gets all the job executions by given ids
     * 
     * @param ids The set of ids
     * @return Returns set of all job executions
     * @throws SchedulerDataException
     */
    @Override
    public List<JobExecution> getExecutionsByIds(List<String> ids) throws SchedulerDataException {
        
        // check if at least one id is given
        if(ids == null || ids.isEmpty()){
            throw new SchedulerDataException("Missing IDs", Arrays.asList("At least one execution ID is required"));
        }
        
        // find all elements with filter
        return this.handle(() -> MongoOps.withinSession(this.transactional, this.client, session -> {
            return this.executions.find(session, in("_id", ids)).into(new ArrayList<>());
        }));
    }
    
    /**
     * Gets the page of executions in the system
     * 
     * @param cluster The optional target cluster to filter
     * @param type The optional type to filter by
     * @param page The page number 
     * @param size The page size
     * @return Returns page of executions
     * @throws SchedulerDataException
     */
    @Override
    public ExecutionsResponse getExecutionsPage(String cluster, String type, int page, int size) throws SchedulerDataException {
        // the target filters
        var filters = new ArrayList<Bson>();
        
        // add type filter if given
        if(!Str.blank(type)){
            filters.add(eq("type", type));
        }
        
        // add cluster filter if given
        if(!Str.blank(cluster)){
            filters.add(eq("cluster", cluster));
        }
        
        // combined filter
        var combined = filters.isEmpty() ? new BsonDocument() : and(filters);
        
        // find all elements with filter
        return this.handle(() -> MongoOps.withinSession(this.transactional, this.client, session -> {
            
            // get filtered page
            var filtered = this.executions
                    .find(session, combined)
                    .sort(descending("name"))
                    .skip(page * size)
                    .limit(size)
                    .into(new ArrayList<>());
            
            // count overall documents in query
            var count = this.executions.countDocuments(session, combined);
            
            return new ExecutionsResponse(filtered, count);
        }));
    }
    
    /**
     * Gets the set of execution index entries based on query
     * 
     * @param cluster The cluster to filter
     * @return Returns set of execution entries
     */
    @Override
    public List<ExecutionIndexEntry> getExecutionIndex(String cluster) throws SchedulerDataException {
        
        // check the cluster
        if(Str.blank(cluster)){
            throw new SchedulerDataException("Invalid Cluster", Arrays.asList("The cluster is required for indexation"));
        }
        
        // the target filters
        var filters = new ArrayList<Bson>();

        // filter by cluster
        filters.add(eq("cluster", cluster));
        
        // find all elements with filter
        return this.handle(() -> MongoOps.withinSession(this.transactional, this.client, session -> {
            
            // get filtered page
            return this.executions
                    .find(session, and(filters), ExecutionIndexEntry.class)
                    .projection(fields(include("_id", "jobId", "status")))
                    .into(new ArrayList<>());
        
        }));
    }
    
    /**
     * Gets the job executions by id
     * 
     * @param id The id of target job execution
     * @return Returns the job execution if found
     * @throws SchedulerDataException
     */
    @Override
    public Optional<JobExecution> getExecutionById(String id) throws SchedulerDataException {
        // id is required
        if(Str.blank(id)){
            throw new SchedulerDataException("Missing Id", Arrays.asList("Execution ID is required"));
        }
        
        return this.handle(() -> MongoOps.withinSession(this.transactional, this.client, session -> {
            return Optional.ofNullable(this.executions.find(session, this.hasId(id)).first());
        }));
    }
    
    /**
     * Inserts new job execution based on input data
     * 
     * @param input The execution input
     * @return Returns created execution instance
     * @throws SchedulerDataException 
     */
    @Override
    public JobExecution insertJobExecution(JobExecutionInput input) throws SchedulerDataException {
        
        // the validation input
        var validation = this.validateExecutionInput(input);
        
        // validation log is not empty
        if(!validation.isEmpty()){
            throw new SchedulerDataException("Invalid Input", validation);
        }
        
        // do within transaction 
        return this.handle(() -> MongoOps.withinSession(this.transactional, this.client, session -> {
            
            // try get job definition
            var jobDefinition = this.definitions.find(session, this.hasId(input.getJobId())).first();
            
            // check if a valid job definition is there to execute
            if(jobDefinition == null){
                throw new SchedulerDataException("Missing Job", Arrays.asList("The Job Definition does not exist"));
            }
            
            // get current time
            var now = new Date();
            
            // generate new id for object
            var newId = ObjectId.get().toHexString();
            
            // build final payload
            var payload = jobDefinition.getPayload();
            
            // if override values are given add to map
            if(input.getPayloadOverride() != null){
                payload = Map.copyOf(jobDefinition.getPayload());
                payload.putAll(input.getPayloadOverride());
            }
            
            // construct new definition to save
            var execution = JobExecution.builder()
                    .id(newId)
                    .jobId(jobDefinition.getId())
                    .name(jobDefinition.getName())
                    .folder(jobDefinition.getFolder())
                    .type(jobDefinition.getType())
                    .status(input.getInitialStatus() == null ? ExecutionStatus.ACTIVE : input.getInitialStatus())
                    .completionSeverity(null)
                    .triggers(jobDefinition.getTriggers())
                    .cluster(Str.blank(input.getCluster()) ? jobDefinition.getCluster() : input.getCluster())
                    .options(jobDefinition.getOptions())
                    .payload(payload)
                    .createdBy(jobDefinition.getCreatedBy())
                    .modifiedBy(jobDefinition.getModifiedBy())
                    .defined(jobDefinition.getCreated())
                    .modified(now)
                    .submited(now)
                    .extra(jobDefinition.getExtra())
                    .build();
            
            // perform insert operation
            var inserted = this.executions.insertOne(session, execution);
            
            // could not insert
            if(inserted.getInsertedId() == null){
                throw new SchedulerDataException("Execution Not Saved", Arrays.asList("The execution was not saved"));
            }
            
            // get saved object if available
            var savedOne = this.executions.find(session, this.hasId(newId)).first();
            
            // something went wrong and saved entity is missing
            if(savedOne == null){
                throw new SchedulerDataException("Execution Missing", Arrays.asList("The execution was not saved"));
            }
            
            // insert new entity
            return savedOne;
        }));   
    }
    
    /**
     * Updates the execution status of the given job instance
     * 
     * @param id The execution id
     * @param input The execution update input
     * @return Returns updated job execution
     * @throws SchedulerDataException 
     */
    @Override
    public JobExecution updateExecution(String id, ExecutionUpdateInput input) throws SchedulerDataException {
        
        // error list
        var errors = new ArrayList<String>();
        
        // use empty input just in case of missing one
        var validInput = input == null ? ExecutionUpdateInput.builder().build() : input;
        
        // check id
        if(Str.blank(id)){
            errors.add("The id of execution is missing");
        }
        
        // check status
        if(validInput.getStatus() == null){
            errors.add("The new status for execution is missing");
        }
        
        // the completion severity
        if(validInput.getStatus() == ExecutionStatus.COMPLETED && validInput.getSeverity() == null){
            errors.add("The completed status requires a valid severity");
        }
        
        // raise error in case of any issue
        if(!errors.isEmpty()){
            throw new SchedulerDataException("Invalid Input", errors);
        }
        
        // do within transaction 
        return this.handle(() -> MongoOps.withinSession(this.transactional, this.client, session -> {
            
            // try get execution to update
            var execution = this.executions.find(session, this.hasId(id)).first();
            
            // check if a execution is missing
            if(execution == null){
                throw new SchedulerDataException("Missing Execution", Arrays.asList("The target execution is missing"));
            }
            
            // when execution is completed we should not allow change of status
            if(execution.getStatus() == ExecutionStatus.COMPLETED && validInput.getStatus() != ExecutionStatus.COMPLETED){
                throw new SchedulerDataException("Wrong Status", Arrays.asList("The completed execution cannot be updated"));
            }
                
            // the update fields
            var updateFields = new HashMap<String, Object>();
            
            // fill in fields
            updateFields.put("modified", new Date());
            updateFields.put("status", validInput.getStatus() == null ? null : validInput.getStatus().name());
            updateFields.put("completionSeverity", validInput.getSeverity() == null ? null : validInput.getSeverity().name());
            
            // the update entity
            var updateEntity = new Document("$set", new Document(updateFields));
            
            // the result of update operation
            var updateResult = this.executions.updateOne(session, this.hasId(id), updateEntity);
            
            // something went wrong while updating
            if(updateResult.getModifiedCount() != 1){
                throw new SchedulerDataException("Execution Update Failed", Arrays.asList("The update of execution failed"));
            }
            
            // get updated entity
            var entity = this.executions.find(session, this.hasId(id)).first();
            
            // something went wrong and updated entity is missing
            if(entity == null){
                throw new SchedulerDataException("Execution Update failed", Arrays.asList("The updated execution was not found"));
            }
            
            return entity;
        }));
    }
    
    /**
     * Deletes the job execution by id
     * 
     * @param id The id of job execution
     * @return Returns removed job execution if any
     * @throws SchedulerDataException
     */
    @Override
    public Optional<JobExecution> deleteExecutionById(String id) throws SchedulerDataException {
        
        // id is required
        if(Str.blank(id)){
            throw new SchedulerDataException("Missing Id", Arrays.asList("Execution ID is required"));
        }
        
        // do within transaction 
        return this.handle(() -> MongoOps.withinSession(this.transactional, this.client, session -> {
            
            // get existing item by id
            var existing = this.executions.find(session, this.hasId(id)).first();
            
            // delete if exists
            if(existing != null) {
                // delete single entity
                this.executions.deleteOne(session, this.hasId(id));
            }
            
            return Optional.ofNullable(existing);
        }));
    }
    
    /**
     * Deletes the executions of the given job id
     * 
     * @param jobId The id of job to filter executions
     * @return Returns number of deleted executions
     * @throws SchedulerDataException
     */
    @Override
    public long deleteExecutionsByJob(String jobId) throws SchedulerDataException {
        
        // id is required
        if(Str.blank(jobId)){
            throw new SchedulerDataException("Missing Job Id", Arrays.asList("Job ID is required"));
        }
        
        // do within transaction 
        return this.handle(() -> MongoOps.withinSession(this.transactional, this.client, session -> {
            return this.executions.deleteMany(session, eq("jobId", jobId)).getDeletedCount();
        }));
    }
    
    /**
     * Deletes all the executions by given status codes
     * 
     * @param statuses The target statuses to delete
     * @return Returns number of deleted executions
     * @throws SchedulerDataException
     */
    @Override
    public long deleteAllExecutionsByStatus(List<ExecutionStatus> statuses) throws SchedulerDataException {
        
        // the deletion filter
        Bson filter;
        
        // no status to filter consider all, otherwise use statuses
        if(statuses == null || statuses.isEmpty()){
            filter = new BsonDocument();
        }
        else{
            filter = in("status", statuses.stream().map(s -> s.name()));
        }
        
        // do within transaction 
        return this.handle(() -> MongoOps.withinSession(this.transactional, this.client, session -> {
            return this.executions.deleteMany(session, filter).getDeletedCount();
        }));
    }
    
    /**
     * Deletes all the executions
     * 
     * @return Returns number of deleted executions
     * @throws SchedulerDataException
     */
    @Override
    public long deleteAllExecutions() throws SchedulerDataException {
        // do within transaction 
        return this.handle(() -> MongoOps.withinSession(this.transactional, this.client, session -> {
            return this.executions.deleteMany(session, new BsonDocument()).getDeletedCount();
        }));
    }
    
    /**
     * Gets all the job iterations 
     * 
     * @return Returns set of all iterations 
     * @throws SchedulerDataException 
     */
    @Override
    public List<Iteration> getAllIterations() throws SchedulerDataException {
        // find all elements with filter
        return this.handle(() -> MongoOps.withinSession(this.transactional, this.client, session -> {
            return this.iterations.find(session, new BsonDocument()).into(new ArrayList<>());
        }));
    }
    
    /**
     * Gets all the job iterations for the given job
     * 
     * @param jobId The job id to filter
     * @return Returns set of all job iterations 
     * @throws SchedulerDataException 
     */
    @Override
    public List<Iteration> getJobIterations(String jobId) throws SchedulerDataException {
        
        // job id is required
        if(Str.blank(jobId)){
            throw new SchedulerDataException("Missing Id", Arrays.asList("Job ID is required"));
        }
        
        // find all elements with filter
        return this.handle(() -> MongoOps.withinSession(this.transactional, this.client, session -> {
            return this.iterations.find(session, eq("jobId", jobId)).into(new ArrayList<>());
        }));
    }
    
    /**
     * Gets all the iterations for the given execution
     * 
     * @param executionId The job id to filter
     * @return Returns set of all job iterations for execution
     * @throws SchedulerDataException
     */
    @Override
    public List<Iteration> getExecutionIterations(String executionId) throws SchedulerDataException {
        
        // execution id is required
        if(Str.blank(executionId)){
            throw new SchedulerDataException("Missing Execution Id", Arrays.asList("Execution ID is required"));
        }
        
        // find all elements with filter
        return this.handle(() -> MongoOps.withinSession(this.transactional, this.client, session -> {
            return this.iterations.find(session, eq("executionId", executionId)).into(new ArrayList<>());
        }));
    }
    
    /**
     * Gets the job iteration by identifier
     * 
     * @param id The job iteration id
     * @return Returns job iteration if found
     * @throws SchedulerDataException
     */
    @Override
    public Optional<Iteration> getIterationById(String id) throws SchedulerDataException {
        
        // id is required
        if(Str.blank(id)){
            throw new SchedulerDataException("Missing Id", Arrays.asList("Iteration ID is required"));
        }
        
        return this.handle(() -> MongoOps.withinSession(this.transactional, this.client, session -> {
            return Optional.ofNullable(this.iterations.find(session, this.hasId(id)).first());
        }));
    }
    
    /**
     * Gets the page of iterations ordered by timestamp (optionally filter by job id and statuses)
     * 
     * @param jobId The job id to filter iterations
     * @param executionId The execution id to filter by
     * @param statuses The set of target statuses to lookup
     * @param page The page number
     * @param size The page size
     * @return Returns a page of iterations with given filter
     * @throws SchedulerDataException
     */
    @Override
    public IterationsResponse getIterationsPage(String jobId, String executionId, List<IterationStatus> statuses, int page, int size) throws SchedulerDataException {
        
        // set of filters
        var filters = new ArrayList<Bson>();
        
        // filter by job id
        if(!Str.blank(jobId)){
            filters.add(eq("jobId", jobId));
        }
        
        // filter by execution id
        if(!Str.blank(executionId)){
            filters.add(eq("executionId", executionId));
        }
        
        // if any status is given to filter
        if(statuses != null && !statuses.isEmpty()){
            filters.add(in("status", statuses.stream().map(s -> s.name())));
        }
        
        // make combined filter
        var combined = filters.isEmpty() ? new BsonDocument() : and(filters);
        
        // find all elements with filter
        return this.handle(() -> MongoOps.withinSession(this.transactional, this.client, session -> {
            
            // get filtered page
            var filtered = this.iterations
                    .find(session, combined)
                    .sort(descending("timestamp"))
                    .skip(page * size)
                    .limit(size)
                    .into(new ArrayList<>());
            
            // count overall documents in query
            var count = this.executions.countDocuments(session, combined);
            
            return new IterationsResponse(filtered, count);
        }));
    }
    
    /**
     * Inserts a job iteration into the data store
     * 
     * @param input The job iteration to save
     * @return Returns saved job iteration
     * @throws SchedulerDataException
     */
    @Override
    public Iteration insertIteration(IterationInput input) throws SchedulerDataException {
        
        // validation log
        var validation = new ArrayList<String>();
        
        // make sure job id is provided
        if(Str.blank(input.getJobId())){
            validation.add("The job id is mandatory for iteration");
        }
        
        // make sure execution id is provided
        if(Str.blank(input.getExecutionId())){
            validation.add("The execution id is mandatory for iteration");
        }
        
        // make sure worker id is provided
        if(Str.blank(input.getWorker())){
            validation.add("The worker name is mandatory for iteration");
        }
        
        // make sure status is given
        if(input.getStatus() == null){
            validation.add("The iteration must have a status");
        }
        
        // in case of any error raise an exception
        if(!validation.isEmpty()){
            throw new SchedulerDataException("Invalid Iteration", validation);
        }
        
        // the new identifier
        var newId = ObjectId.get().toHexString();
        
        // do within transaction 
        return this.handle(() -> MongoOps.withinSession(this.transactional, this.client, session -> {
            
            // build new iteration to save
            var iteration = Iteration.builder()
                    .id(newId)
                    .jobId(input.getJobId())
                    .executionId(input.getExecutionId())
                    .worker(input.getWorker())
                    .status(input.getStatus())
                    .message(input.getMessage())
                    .payload(input.getPayload())
                    .runtime(input.getRuntime())
                    .timestamp(input.getTimestamp() == null ? new Date() : input.getTimestamp())
                    .build();
            
            // perform insert operation
            var inserted = this.iterations.insertOne(session, iteration);
            
            // could not insert
            if(inserted.getInsertedId() == null){
                throw new SchedulerDataException("Iteration Not Saved", Arrays.asList("The iteratoin was not saved"));
            }
            
            // get saved object if available
            var savedOne = this.iterations.find(session, this.hasId(newId)).first();
            
            // something went wrong and saved entity is missing
            if(savedOne == null){
                throw new SchedulerDataException("Iteration Missing", Arrays.asList("The iteration was not saved"));
            }
            
            // return inserted
            return savedOne;
        }));   
    }
    
    /**
     * Deletes an entry by id and returns deleted one
     * 
     * @param id The id of job iteration to delete
     * @return Returns deleted job iteration item
     * @throws SchedulerDataException
     */
    @Override
    public Optional<Iteration> deleteIterationById(String id) throws SchedulerDataException {
        
        // iteration id is required
        if(Str.blank(id)){
            throw new SchedulerDataException("Missing Iteration Id", Arrays.asList("Iteration ID is required"));
        }

        // do within transaction 
        return this.handle(() -> MongoOps.withinSession(this.transactional, this.client, session -> {
            
            // get existing item by id
            var existing = this.iterations.find(session, this.hasId(id)).first();
            
            // delete if exists
            if(existing != null) {
                // delete single entity
                this.iterations.deleteOne(session, this.hasId(id));
            }
            
            return Optional.ofNullable(existing);
        }));
    }
    
    /**
     * Deletes all the iterations for the given job id
     * 
     * @param jobId The target job id
     * @return Returns number of removed job iteration entries
     * @throws SchedulerDataException
     */
    @Override
    public long deleteJobIterations(String jobId) throws SchedulerDataException {
        
        // job id is required
        if(Str.blank(jobId)){
            throw new SchedulerDataException("Missing Job Id", Arrays.asList("Job ID is required"));
        }
        
        // do within transaction 
        return this.handle(() -> MongoOps.withinSession(this.transactional, this.client, session -> {
            return this.iterations.deleteMany(session, eq("jobId", jobId)).getDeletedCount();
        }));
    }
    
    /**
     * Deletes all the iterations for the given execution id
     * 
     * @param executionId The target execution id
     * @return Returns number of removed execution iteration entries
     * @throws SchedulerDataException
     */
    @Override
    public long deleteExecutionIterations(String executionId) throws SchedulerDataException {
        
        // execution id is required
        if(Str.blank(executionId)){
            throw new SchedulerDataException("Missing Id", Arrays.asList("Execution ID is required"));
        }
        
        // do within transaction 
        return this.handle(() -> MongoOps.withinSession(this.transactional, this.client, session -> {
            return this.iterations.deleteMany(session, eq("executionId", executionId)).getDeletedCount();
        }));
    }
    
    /**
     * Deletes all the iterations
     * 
     * @return Returns number of deleted records
     * @throws SchedulerDataException
     */
    @Override
    public long deleteAllIterations() throws SchedulerDataException {
        // do within transaction 
        return this.handle(() -> MongoOps.withinSession(this.transactional, this.client, session -> {
            return this.iterations.deleteMany(session, new BsonDocument()).getDeletedCount();
        }));
    }
    
    /**
     * Deletes all the iterations before given timestamp
     * 
     * @param timestamp The timestamp to filter
     * @return Returns number of deleted items
     * @throws SchedulerDataException
     */
    @Override
    public long deleteIterationsBefore(Date timestamp) throws SchedulerDataException {
        
        // timestamp is required
        if(timestamp == null){
            throw new SchedulerDataException("Missing Timestamp", Arrays.asList("The timestamp is required"));
        }
        
        // do within transaction 
        return this.handle(() -> MongoOps.withinSession(this.transactional, this.client, session -> {
            return this.iterations.deleteMany(session, lt("timestamp", timestamp)).getDeletedCount();
        }));
    }
    
    /**
     * Gets all the clusters
     * 
     * @return Returns set of all clusters
     * @throws SchedulerDataException
     */
    @Override
    public List<ClusterDefinition> getAllClusters() throws SchedulerDataException {
        // find all elements with filter
        return this.handle(() -> MongoOps.withinSession(this.transactional, this.client, session -> {
            return this.clusters.find(session, new BsonDocument()).into(new ArrayList<>());
        }));
    }
        
    /**
     * Gets the cluster by identifier
     * 
     * @param id The cluster definition id
     * @return Returns cluster definition if found
     * @throws SchedulerDataException
     */
    @Override
    public Optional<ClusterDefinition> getClusterById(String id) throws SchedulerDataException {
        
        // id is required
        if(Str.blank(id)){
            throw new SchedulerDataException("Missing Id", Arrays.asList("Cluster ID is required"));
        }
        
        // find all elements with filter
        return this.handle(() -> MongoOps.withinSession(this.transactional, this.client, session -> {
            return Optional.ofNullable(this.clusters.find(session, this.hasId(id)).first());
        }));
    }
   
    /**
     * Join the worker to the cluster
     * 
     * @param input The worker joining input
     * @return Returns result of operation
     * @throws SchedulerDataException
     */
    @Override
    public ClusterWorker joinWorker(WorkerJoinInput input) throws SchedulerDataException {
        
        // validation log
        var validation = new ArrayList<String>();
      
        // make sure cluster value is fine
        if(Str.blank(input.getCluster()) || !Clusters.CLUSTER_REGEX.asMatchPredicate().test(input.getCluster())){
            validation.add("The cluster value is missing or invalid");
        }
        
        // make sure worker value is fine
        if(Str.blank(input.getName()) || !Clusters.WORKER_REGEX.asMatchPredicate().test(input.getName())){
            validation.add("The worker name value is missing or invalid");
        }
        
        // make sure max idle time is a positive value
        if(input.getMaxIdle() <= 0){
            validation.add("The maximum idle time should be a positive value");
        }
        
        // kind must be provided
        if(input.getKind() == null){
            validation.add("The worker kind should be provided");
        }
        
        // in case of any issue raise an exception
        if(!validation.isEmpty()){
            throw new SchedulerDataException("Invalid Worker", validation);
        }
        
        // the cluster filter
        var clusterFilter = eq("cluster", input.getCluster());
        
        // do within transaction 
        return this.handle(() -> MongoOps.withinSession(this.transactional, this.client, session -> {
            
            // try get existing cluster
            var cluster = this.clusters.find(session, clusterFilter).first();
            
            // check if no cluster with mentioned name
            if(cluster == null){
                cluster = this.insertCluster(session, input);
            }
            
            // return updated
            return this.joinWorker(session, cluster, input);
        })); 
    }
    
    /**
     * Updates a worker in the data store
     * 
     * @param heartbeat The heartbeat to update
     * @return Returns saved worker 
     * @throws SchedulerDataException
     */
    @Override
    public ClusterWorker updateWorker(WorkerHeartbeat heartbeat) throws SchedulerDataException {
        // raise error in case missing cluster
        if(Str.blank(heartbeat.getCluster())){
            throw new SchedulerDataException("Cannot update", Arrays.asList("Missing cluster name"));
        }
        
        // raise error in case of worker name
        if(Str.blank(heartbeat.getCluster())){
            throw new SchedulerDataException("Cannot update", Arrays.asList("Missing worker name"));
        }
        
        // the target activity to update (heartbeat by default)
        var activity = heartbeat.getActivity() == null ? WorkerActivity.HEARTBEAT : heartbeat.getActivity();
        
        // do within transaction 
        return this.handle(() -> MongoOps.withinSession(this.transactional, this.client, session -> {
            
            // get the cluster from storage
            var cluster = this.clusters.find(session, eq("cluster", heartbeat.getCluster())).first();
            
            // check if cluster is there
            if(cluster == null){
                throw new SchedulerDataException("Cannot update", Arrays.asList("Cluster with given name does not exist"));
            }
            
            // get existing workers
            List<ClusterWorker> workers = Lang.or(cluster.getWorkers(), () -> new ArrayList<>());
            
            // process all workers
            var worker = workers.stream().filter(w -> w.getName().equals(heartbeat.getName())).findFirst();
            
            // no worker with such name
            if(!worker.isPresent()){
                throw new SchedulerDataException("Cannot update", Arrays.asList("Worker with given name does not exist in cluster"));
            }
            
            // the current time
            var now = new Date();
            
            // update cluster time
            cluster.setUpdated(now);
            
            // update worker
            worker.get().setUpdated(now);
            worker.get().setActivity(activity);
            
            // update the cluster
            this.updateCluster(session, cluster);
            
            return worker.get();
        }));
    }
              
    /**
     * Deletes an entry by id and returns deleted one
     * 
     * @param id The id of cluster to delete
     * @return Returns deleted cluster item
     * @throws SchedulerDataException
     */
    @Override
    public Optional<ClusterDefinition> deleteClusterById(String id) throws SchedulerDataException {
        
        // iteration id is required
        if(Str.blank(id)){
            throw new SchedulerDataException("Missing Cluster Id", Arrays.asList("Cluster ID is required"));
        }

        // do within transaction 
        return this.handle(() -> MongoOps.withinSession(this.transactional, this.client, session -> {
            
            // get existing item by id
            var existing = this.clusters.find(session, this.hasId(id)).first();
            
            // delete if exists
            if(existing != null) {
                // delete single entity
                this.clusters.deleteOne(session, this.hasId(id));
            }
            
            return Optional.ofNullable(existing);
        }));
    }
    
    /**
     * Deletes all the clusters in the system
     * 
     * @return Returns number of deleted items
     * @throws SchedulerDataException
     */
    @Override
    public long deleteAllClusters() throws SchedulerDataException {
        // do within transaction 
        return this.handle(() -> MongoOps.withinSession(this.transactional, this.client, session -> {
            return this.clusters.deleteMany(session, new BsonDocument()).getDeletedCount();
        }));
    }
    
    /**
     * Builds the collection name
     * 
     * @param name The collection name 
     * @return Returns collection name with prefix
     */
    protected final String collection(String name){
        return String.format("%s_%s", COLLECTION_PREFIX, name);
    } 
    
    /**
     * Validates the definition input
     * 
     * @param input The input to validate
     * @return Returns validation messages
     */
    protected List<String> validateDefinitionInput(JobDefinitionInput input){
        
        // the set of issues
        var issues = new ArrayList<String>();
        
        // name does not match the required criteria
        if(Str.blank(input.getName()) || !Jobs.NAME_REGEX.asMatchPredicate().test(input.getName())){
            issues.add("The job name is blank or contains invalid characters");
        }
        
        // folder does not match the required criteria
        if(Str.blank(input.getFolder()) || !Jobs.FOLDER_REGEX.asMatchPredicate().test(input.getFolder())){
            issues.add("The job folder is blank or contains invalid characters");
        }
        
        // cluster does not match the required criteria
        if(Str.blank(input.getCluster()) || !Clusters.CLUSTER_REGEX.asMatchPredicate().test(input.getCluster())){
            issues.add("The job cluster is blank or contains invalid characters");
        }
        
        // type does not match the required criteria
        if(Str.blank(input.getType()) || !Jobs.TYPE_REGEX.asMatchPredicate().test(input.getType())){
            issues.add("The job type is blank or contains invalid characters");
        }
        
        // unique trigger names
        var triggerNames = new HashSet<String>();
        
        // get triggers to check
        var triggers = input.getTriggers();
        
        // empty list for safety
        if(triggers == null){
            triggers = Arrays.asList();
        }
        
        // validate each trigger
        triggers.forEach(trigger -> {
            
            // trigger name does not match the required criteria
            if(Str.blank(trigger.getName()) || !Jobs.NAME_REGEX.asMatchPredicate().test(trigger.getName())){
                issues.add("The trigger name is blank or contains invalid characters");
            }
            
            // multiple triggers with same name
            if(!triggerNames.add(trigger.getName())){
                issues.add(String.format("The trigger with name %s is defined more than once", trigger.getName()));
            }
            
            // validate cron expression to be present in case of cron trigger
            if(trigger.getType() == TriggerType.CRON && Str.blank(trigger.getCron())){
                issues.add(String.format("The cron trigger %s should have a valid cron expression", trigger.getCron()));
            }
        });
        
        return issues;
    }
    
    /**
     * Validates the execution input
     * 
     * @param input The input to validate
     * @return Returns validation messages
     */
    protected List<String> validateExecutionInput(JobExecutionInput input){
        
        // the set of issues
        var issues = new ArrayList<String>();
        
        // if job id not given then is not valid
        if(Str.blank(input.getJobId())){
            issues.add("The job id is mandatory to execute");
        }
        
        // if cluster override value is given but is malformed report issue
        if(!Str.blank(input.getCluster()) && !Clusters.CLUSTER_REGEX.asMatchPredicate().test(input.getCluster())){
            issues.add("The job cluster override value is given but contains invalid characters");
        }
        
        return issues;
    }

    /**
     * Update the existing job definition with given input
     * @param session The session context of client
     * @param existing The existing instance
     * @param input The input to update
     * @return Returns updated entity
     */
    protected JobDefinition updateJobImpl(ClientSession session, JobDefinition existing, JobDefinitionInput input) throws SchedulerDataException {
        
        // build the entity to perform update operation
        var toUpdate = existing.toBuilder()
                .id(existing.getId())
                .name(input.getName())
                .folder(input.getFolder())
                .type(input.getType())
                .cluster(input.getCluster())
                .triggers(input.getTriggers())
                .options(input.getOptions())
                .selectors(input.getSelectors())
                .payload(input.getPayload())
                .extra(input.getExtra())
                .modifiedBy(input.getModifiedBy())
                .modified(new Date())
                .build();
        
        // perform replace operation and get result
        this.definitions.replaceOne(session, this.hasId(existing.getId()), toUpdate);
        
        // try get updated item
        var updated = this.definitions.find(session, this.hasId(existing.getId())).first();
    
        // updated entity does not exist
        if(updated == null){
            throw new SchedulerDataException("Update Failed", Arrays.asList("The updated entity does not exist"));
        }
        
        return updated;
    }
    
    /**
     * Handle the exception simply by throwing
     * 
     * @param <T> The result type
     * @param fn The function to apply
     * @return Returns the function result
     * @throws SchedulerDataException 
     */
    protected <T> T handle(Supplier<T> fn) throws SchedulerDataException {
        try{
            return fn.get();
        }
        catch(SchedulerDataException e){
            throw e;
        }
        catch(Throwable e){
            log.error("Mongo Error", e);
            throw new SchedulerDataException(e);
        }
    }

    /**
     * Builds the identity filter
     * 
     * @param id The id to filter by
     * @return Returns filter by id
     */
    protected Bson hasId(String id){
        return eq("_id", id);
    }

    
    /**
     * Creates a new cluster and inserts into the system
     * 
     * @param session The client session
     * @param input The cluster input
     * @return Returns created cluster 
     */
    protected ClusterDefinition insertCluster(ClientSession session, WorkerJoinInput input) throws SchedulerDataException {

        // new entity id
        var newId = ObjectId.get().toHexString();

        // the now time
        var now = new Date();
        
        // create new cluster to save
        var newCluster = ClusterDefinition.builder()
                .id(newId)
                .cluster(input.getCluster())
                .maxIdle(input.getMaxIdle())
                .workers(new ArrayList<>())
                .created(now)
                .updated(now)
                .build();

        // try insert
        var insertResult = this.clusters.insertOne(session, newCluster);

        // check insert result
        if(insertResult == null || insertResult.getInsertedId() == null){
            throw new SchedulerDataException("Cluster Error", Arrays.asList("Cluster was not saved due to internal issue"));
        }
        
        // get inserted object back
        var existingCluster = this.clusters.find(session, this.hasId(newId)).first();

        // could not save cluster, something went wrong
        if(existingCluster == null){
            throw new SchedulerDataException("Cluster Error", Arrays.asList("Could not save cluster for the first time"));
        }
        
        return existingCluster;
    }
    
    /**
     * Joins the given worker to an existing cluster
     * 
     * @param session The client session 
     * @param cluster The existing cluster to update
     * @param input The worker join input
     * @return Returns joined worker
     */
    protected ClusterWorker joinWorker(ClientSession session, ClusterDefinition cluster, WorkerJoinInput input) {

        // the current time
        var now = new Date();
 
        // the max idle time
        var maxIdle = cluster.getMaxIdle();
        
        // a new worker builder for new instance
        var workerBuilder = ClusterWorker.builder()
                .name(input.getName())
                .kind(input.getKind())
                .cluster(cluster.getCluster())
                .session(Str.random(16))
                .persistence(input.isPersistence())
                .persistenceMethod(input.getPersistenceMethod())
                .maxIdle(maxIdle)
                .updated(now);
        
        // the list of workers 
        List<ClusterWorker> workers = Lang.or(cluster.getWorkers(), () -> Arrays.asList());
        
        // get all other worker entities (having other names)
        var newWorkers = workers.stream().filter(w -> !w.getName().equals(input.getName())).collect(Collectors.toList());
        
        // new cluster builder to update
        var updatedCluster = cluster.toBuilder()
                .workers(newWorkers)
                .updated(now)
                .build();
        
        // get worker with given name if any
        var existing = workers.stream().filter(w -> w.getName().equals(input.getName())).findFirst();
        
        // if input worker wants to be an exclusive worker in cluster
        if(input.getKind() == WorkerKind.EXCLUSIVE){
            
            // check if any active worker is there
            var anyActive = workers.stream().anyMatch(w -> !this.isIdle(w, now, maxIdle));
            
            // there is an active one so cannot add exclusive one
            if(anyActive){
                throw new SchedulerDataException("Worker Error", Arrays.asList("Cannot join exclusive worker as there is already an active worker."));
            }
            
            // finalize exclusive worker
            var finalWorker = workerBuilder.build();
            
            // add exclusive worker to final workers list
            newWorkers.add(finalWorker);
            
            // save updated cluster
            this.updateCluster(session, updatedCluster);
            
            return finalWorker;
        }
        
        // indicate if there is any active exclusive item
        var anyActiveExclusive = workers.stream().anyMatch(w -> !this.isIdle(w, now, maxIdle) && w.getKind() == WorkerKind.EXCLUSIVE);
        
        // in case if cluster has exclusive active worker we cannot add more
        if(anyActiveExclusive){
            throw new SchedulerDataException("Worker Error", Arrays.asList("Cannot join any worker as there is an exclusive active worker"));
        }
        
        // if input is a balanced worker then we will add with unique name
        if(input.getKind() == WorkerKind.BALANCED){
            
            // add a random postfix for the new balanced worker name
            workerBuilder.name(String.format("%s-%s", input.getName(), Str.random(8)));
            
            // finalize balanced worker
            var finalWorker = workerBuilder.build();
            
            // add balanced worker to final workers list
            newWorkers.add(finalWorker);
            
            // save updated cluster
            this.updateCluster(session, updatedCluster);
            
            return finalWorker;
        }
        
        // if joining worker is of kind replica 
        if(input.getKind() == WorkerKind.REPLICA){
            
            // if there is an existing non-idle (active) worker with same name do not replicate
            if(existing.isPresent() && !this.isIdle(existing.get(), now, maxIdle)){
                throw new SchedulerDataException("Worker Error", Arrays.asList("An active worker with same name already exists"));
            }
            
            // finalize replica worker
            var finalWorker = workerBuilder.build();
            
            // add replica worker to final workers list
            newWorkers.add(finalWorker);
            
            // save updated cluster
            this.updateCluster(session, updatedCluster);
            
            return finalWorker;
        }      
        
        // no supported otherwise
        throw new SchedulerDataException("Worker Error", Arrays.asList("The worker kind is not supported"));
    }
    
    /**
     * Checks if the worker is idle as of now
     * 
     * @param worker The worker to check
     * @param now The now time to consider
     * @param maxIdle The maximum idle time in cluster
     * @return Returns if idle
     */
    protected boolean isIdle(ClusterWorker worker, Date now, long maxIdle){
        
        // updated instant
        var updated = worker.getUpdated().toInstant().toEpochMilli();
        
        // current instant
        var current = now.toInstant().toEpochMilli();
        
        // if updated time considering maximum idle time is before current time consider as idle
        return updated + maxIdle < current;
    }

    /**
     * Updates existing cluster definition in the storage
     * 
     * @param session The client session
     * @param cluster The updated cluster
     * @return Returns the updated cluster
     */
    protected ClusterDefinition updateCluster(ClientSession session, ClusterDefinition cluster) {

        // perform replace operation and get result
        this.clusters.replaceOne(session, this.hasId(cluster.getId()), cluster);
        
        // try get updated item
        var updated = this.clusters.find(session, this.hasId(cluster.getId())).first();
    
        // updated entity does not exist
        if(updated == null){
            throw new SchedulerDataException("Cluster Update Failed", Arrays.asList("The cluster is not updated due to internal error"));
        }
        
        return updated;
    }
}
