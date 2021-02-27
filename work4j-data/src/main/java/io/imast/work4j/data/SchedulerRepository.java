package io.imast.work4j.data;

import io.imast.work4j.data.exception.SchedulerDataException;
import io.imast.work4j.model.JobDefinition;
import io.imast.work4j.model.JobDefinitionInput;
import io.imast.work4j.model.JobRequestResult;
import io.imast.work4j.model.execution.ExecutionStatus;
import io.imast.work4j.model.execution.ExecutionUpdateInput;
import io.imast.work4j.model.execution.ExecutionsResponse;
import io.imast.work4j.model.execution.JobExecution;
import io.imast.work4j.model.execution.JobExecutionInput;
import io.imast.work4j.model.iterate.Iteration;
import io.imast.work4j.model.iterate.IterationInput;
import io.imast.work4j.model.iterate.IterationStatus;
import io.imast.work4j.model.iterate.IterationsResponse;
import io.imast.work4j.model.worker.WorkerActivity;
import io.imast.work4j.model.worker.WorkerSession;
import io.imast.work4j.model.worker.WorkerSessionInput;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * The scheduler data repository
 * 
 * @author davitp
 */
public interface SchedulerRepository {
    
    /**
     * Ensures that schema is ready for data operations
     * 
     * @throws SchedulerDataException 
     */
    public void ensureSchema() throws SchedulerDataException;
    
    /**
     * Gets all the job definitions
     * 
     * @param tenant The target tenant
     * @param cluster The cluster to filter
     * @param type The type of jobs
     * @return Returns set of all job definitions
     * @throws SchedulerDataException
     */
    public List<JobDefinition> getAllJobs(String tenant, String cluster, String type) throws SchedulerDataException;
    
    /**
     * Gets the job definition by identifier
     * 
     * @param id The job definition id
     * @return Returns job definition if found
     * @throws SchedulerDataException
     */
    public Optional<JobDefinition> getJobById(String id) throws SchedulerDataException;
    
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
     * @param input The execution update input
     * @return Returns updated job execution
     * @throws SchedulerDataException 
     */
    public JobExecution updateExecution(String id, ExecutionUpdateInput input) throws SchedulerDataException;
    
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
    public IterationsResponse getIterationsPage(String jobId, String executionId, List<IterationStatus> statuses, int page, int size) throws SchedulerDataException;
    
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
    public Optional<WorkerSession> getWorkerSessionById(String id) throws SchedulerDataException;
    
    /**
     * Inserts a agent definition into the data store
     * 
     * @param input The session to insert
     * @return Returns saved worker session
     * @throws SchedulerDataException
     */
    public WorkerSession insertWorkerSession(WorkerSessionInput input) throws SchedulerDataException;
    
    /**
     * Updates a agent definition in the data store
     * 
     * @param id The id of worker session
     * @param activity The activity to modify
     * @return Returns saved worker session
     * @throws SchedulerDataException
     */
    public WorkerSession updateWorkerSession(String id, WorkerActivity activity) throws SchedulerDataException;
    
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
}
