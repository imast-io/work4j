package io.imast.work4j.data.impl;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import io.imast.core.Str;
import io.imast.work4j.data.SchedulerRepository;
import io.imast.work4j.data.exception.SchedulerDataException;
import io.imast.work4j.model.JobDefinition;
import io.imast.work4j.model.JobDefinitionInput;
import io.imast.work4j.model.JobRequestResult;
import io.imast.work4j.model.execution.ExecutionStatus;
import io.imast.work4j.model.execution.ExecutionsResponse;
import io.imast.work4j.model.execution.JobExecution;
import io.imast.work4j.model.execution.JobExecutionInput;
import io.imast.work4j.model.iterate.Iteration;
import io.imast.work4j.model.iterate.IterationInput;
import io.imast.work4j.model.iterate.IterationStatus;
import io.imast.work4j.model.iterate.JobIterationsResult;
import io.imast.work4j.model.worker.WorkerSession;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;

/**
 * The mongo repository for the scheduler data
 * 
 * @author davitp
 */
public class SchedulerMongoRepisotory implements SchedulerRepository {

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
     * The workers collection
     */
    private final MongoCollection<WorkerSession> workers;
    
    /**
     * The executions collection
     */
    private final MongoCollection<JobExecution> executions;
    
    /**
     * Creates new instance of scheduler mongo repository
     * 
     * @param client The client to mongo
     * @param mongoDatabase The mongo database reference
     */
    public SchedulerMongoRepisotory(MongoClient client, MongoDatabase mongoDatabase){
        this.client = client;
        this.mongoDatabase = mongoDatabase;
        this.definitions = this.mongoDatabase.getCollection(this.collection("definitions"), JobDefinition.class);
        this.iterations = this.mongoDatabase.getCollection(this.collection("iterations"), Iteration.class);
        this.workers = this.mongoDatabase.getCollection(this.collection("workers"), WorkerSession.class);
        this.executions = this.mongoDatabase.getCollection(this.collection("executions"), JobExecution.class);

    }
    
    /**
     * Ensures that schema is ready for data operations
     * 
     * @throws SchedulerDataException 
     */
    @Override
    public void ensureSchema() throws SchedulerDataException {        
    }
    
    /**
     * Gets all the job definitions
     * 
     * @param tenant The target tenant
     * @param cluster The cluster to filter
     * @param type The type of jobs
     * @return Returns set of all job definitions
     * @throws SchedulerDataException
     */
    @Override
    public List<JobDefinition> getAllJobs(String tenant, String cluster, String type) throws SchedulerDataException {
        
        // the target filters
        var filters = new ArrayList<Bson>();
        
        // add type filter if given
        if(!Str.blank(type)){
            filters.add(eq("type", type));
        }
        
        // add tenant filter if given
        if(!Str.blank(tenant)){
            filters.add(eq("tenant", tenant));
        }
        
        // add cluster filter if given
        if(!Str.blank(cluster)){
            filters.add(eq("cluster", cluster));
        }
        
        // combined filter
        var combined = filters.isEmpty() ? new BsonDocument() : and(filters);

        // find all elements with filter
        return this.handle(() -> MongoOps.withTransaction(this.client, session -> {
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
        return this.handle(() -> MongoOps.withTransaction(this.client, session -> {
            return Optional.ofNullable(this.definitions.find(session, eq("_id", id)).first());
        }));
        
    }
    
    /**
     * Get the page of job definitions sorted by code
     * 
     * @param tenant The target tenant
     * @param cluster The cluster to filter
     * @param type The type of jobs
     * @param page The page number
     * @param size The page size
     * @return Returns a page of job definitions
     * @throws SchedulerDataException
     */
    public JobRequestResult getJobPage(String tenant, String cluster, String type, int page, int size) throws SchedulerDataException;
    
    /**
     * Saves a job definition into the data store
     * 
     * @param definitionInput The job definition input to save
     * @param replace The optional flag that allows to replace
     * @return Returns saved job definition
     * @throws SchedulerDataException
     */
    public JobDefinition insertJob(JobDefinitionInput definitionInput, boolean replace) throws SchedulerDataException;
    
    /**
     * Updates an existing job definition 
     * 
     * @param id The job definition id
     * @param definitionInput The job definition input to update
     * @return Returns saved job definition
     * @throws SchedulerDataException
     */
    public JobDefinition updateJob(String id, JobDefinitionInput definitionInput) throws SchedulerDataException;
    
    /**
     * Deletes an entry by id and returns deleted one
     * 
     * @param id The id of job definition to delete
     * @return Returns deleted job definition item
     * @throws SchedulerDataException
     */
    public Optional<JobDefinition> deleteJobById(String id) throws SchedulerDataException;
    
    /**
     * Deletes an entry by location
     * 
     * @param folder The folder of target job
     * @param name The name of job in folder
     * @return Returns deleted job definition item
     * @throws SchedulerDataException
     */
    public Optional<JobDefinition> deleteJobByPath(String folder, String name) throws SchedulerDataException;
    
    /**
     * Deletes all the records
     * 
     * @return Returns number of deleted records
     * @throws SchedulerDataException
     */
    public long deleteAllJobs() throws SchedulerDataException;
    
    /**
     * Gets all the job executions
     * 
     * @param tenant The tenant to filter
     * @param cluster The optional target cluster to filter
     * @param type The optional type to filter by 
     * @return Returns set of all job executions
     * @throws SchedulerDataException
     */
    public List<JobExecution> getAllExecutions(String tenant, String cluster, String type) throws SchedulerDataException;
    
    /**
     * Gets all the job executions of job
     * 
     * @param jobId The job id to filter
     * @return Returns set of all job executions
     * @throws SchedulerDataException
     */
    public List<JobExecution> getExecutionsByJob(String jobId) throws SchedulerDataException;
    
    /**
     * Gets the page of executions in the system
     * 
     * @param tenant The tenant to filter
     * @param cluster The optional target cluster to filter
     * @param type The optional type to filter by
     * @param page The page number 
     * @param size The page size
     * @return Returns page of executions
     * @throws SchedulerDataException
     */
    public ExecutionsResponse getExecutionsPage(String tenant, String cluster, String type, int page, int size) throws SchedulerDataException;
    
    /**
     * Gets the job executions by id
     * 
     * @param id The id of target job execution
     * @return Returns the job execution if found
     * @throws SchedulerDataException
     */
    public Optional<JobExecution> getExecutionById(String id) throws SchedulerDataException;
    
    /**
     * Gets all the job iterations 
     * 
     * @return Returns set of all iterations 
     * @throws SchedulerDataException 
     */
    public List<Iteration> getAllIterations() throws SchedulerDataException;
    
    /**
     * Gets all the job iterations for the given job
     * 
     * @param jobId The job id to filter
     * @return Returns set of all job iterations 
     * @throws SchedulerDataException 
     */
    public List<Iteration> getJobIterations(String jobId) throws SchedulerDataException;
    
    /**
     * Gets all the iterations for the given execution
     * 
     * @param executionId The job id to filter
     * @return Returns set of all job iterations for execution
     * @throws SchedulerDataException
     */
    public List<Iteration> getExecutionIterations(String executionId) throws SchedulerDataException;
    
    /**
     * Inserts new job execution based on input data
     * 
     * @param executionInput The execution input
     * @return Returns created execution instance
     * @throws SchedulerDataException 
     */
    public JobExecution insertJobExecution(JobExecutionInput executionInput) throws SchedulerDataException;
    
    /**
     * Deletes the job execution by id
     * 
     * @param id The id of job execution
     * @return Returns removed job execution if any
     * @throws SchedulerDataException
     */
    public Optional<JobExecution> deleteExecutionById(String id) throws SchedulerDataException;
    
    /**
     * Deletes the executions of the given job id
     * 
     * @param jobId The id of job to filter executions
     * @return Returns number of deleted executions
     * @throws SchedulerDataException
     */
    public long deleteExecutionsByJob(String jobId) throws SchedulerDataException;
    
    /**
     * Deletes all the executions by given status codes
     * 
     * @param statuses The target statuses to delete
     * @return Returns number of deleted executions
     * @throws SchedulerDataException
     */
    public long deleteAllExecutionsByStatus(List<ExecutionStatus> statuses) throws SchedulerDataException;
    
    /**
     * Deletes all the executions
     * 
     * @return Returns number of deleted executions
     * @throws SchedulerDataException
     */
    public long deleteAllExecutions() throws SchedulerDataException;
    
    /**
     * Updates the execution status of the given job instance
     * 
     * @param id The execution id
     * @param status The new execution status
     * @return Returns updated job execution
     * @throws SchedulerDataException 
     */
    public JobExecution updateExecutionStatus(String id, ExecutionStatus status) throws SchedulerDataException;
    
    /**
     * Gets the job iteration by identifier
     * 
     * @param id The job iteration id
     * @return Returns job iteration if found
     * @throws SchedulerDataException
     */
    public Optional<Iteration> getIterationById(String id) throws SchedulerDataException;
    
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
    public JobIterationsResult getIterationsPage(String jobId, String executionId, List<IterationStatus> statuses, int page, int size) throws SchedulerDataException;
    
    /**
     * Inserts a job iteration into the data store
     * 
     * @param iterationInput The job iteration to save
     * @return Returns saved job iteration
     * @throws SchedulerDataException
     */
    public Iteration insertIteration(IterationInput iterationInput) throws SchedulerDataException;
    
    /**
     * Deletes an entry by id and returns deleted one
     * 
     * @param id The id of job iteration to delete
     * @return Returns deleted job iteration item
     * @throws SchedulerDataException
     */
    public Optional<Iteration> deleteIterationById(String id) throws SchedulerDataException;
    
    /**
     * Deletes all the iterations for the given job id
     * 
     * @param jobId The target job id
     * @return Returns number of removed job iteration entries
     * @throws SchedulerDataException
     */
    public long deleteJobIterations(String jobId) throws SchedulerDataException;
    
    /**
     * Deletes all the iterations for the given execution id
     * 
     * @param executionId The target execution id
     * @return Returns number of removed execution iteration entries
     * @throws SchedulerDataException
     */
    public long deleteExecutionIterations(String executionId) throws SchedulerDataException;
    
    /**
     * Deletes all the iterations
     * 
     * @return Returns number of deleted records
     * @throws SchedulerDataException
     */
    public long deleteAllIterations() throws SchedulerDataException;
    
    /**
     * Deletes all the iterations before given timestamp
     * 
     * @param timestamp The timestamp to filter
     * @return Returns number of deleted items
     * @throws SchedulerDataException
     */
    public long deleteIterationsBefore(Date timestamp) throws SchedulerDataException;
    
    /**
     * Gets all the worker sessions
     * 
     * @return Returns set of all worker sessions
     * @throws SchedulerDataException
     */
    public List<WorkerSession> getAllWorkerSessions() throws SchedulerDataException;
    
    /**
     * Gets the set of worker sessions within a cluster
     * 
     * @param cluster The cluster to filter
     * @return Returns set of cluster worker sessions
     * @throws SchedulerDataException
     */
    public List<WorkerSession> getAllWorkerSessions(String cluster) throws SchedulerDataException;
    
    /**
     * Gets the worker session by identifier
     * 
     * @param id The agent definition id
     * @return Returns agent definition if found
     * @throws SchedulerDataException
     */
    public Optional<WorkerSession> getWorkerSessionId(String id) throws SchedulerDataException;
    
    /**
     * Inserts a agent definition into the data store
     * 
     * @param session The session to insert
     * @return Returns saved agent definition
     * @throws SchedulerDataException
     */
    public WorkerSession upsertWorkerSession(WorkerSession session) throws SchedulerDataException;
    
    /**
     * Deletes all the idle sessions for the given cluster and machine
     * 
     * @param cluster The cluster to filter
     * @param name The name of machine to remove
     * @return Returns number of deleted sessions
     * @throws SchedulerDataException
     */
    public long deleteIdleWorkerSessions(String cluster, String name) throws SchedulerDataException;
    
    /**
     * Deletes all the sessions for the given cluster and machine
     * 
     * @param cluster The cluster to filter
     * @param name The name of machine to remove
     * @return Returns number of deleted sessions
     * @throws SchedulerDataException
     */
    public long deleteWorkerSessions(String cluster, String name) throws SchedulerDataException;
       
    /**
     * Deletes an entry by id and returns deleted one
     * 
     * @param id The id of agent definition to delete
     * @return Returns deleted agent definition item
     * @throws SchedulerDataException
     */
    public Optional<WorkerSession> deleteWorkerSessionById(String id) throws SchedulerDataException;
    
    /**
     * Builds the collection name
     * 
     * @param name The collection name 
     * @return Returns collection name with prefix
     */
    private String collection(String name){
        return String.format("%s_%s", COLLECTION_PREFIX, name);
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
        catch(Throwable e){
            throw new SchedulerDataException(e);
        }
    }
}