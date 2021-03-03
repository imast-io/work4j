package io.imast.work4j.controller;

import io.imast.work4j.data.SchedulerDataRepository;
import io.imast.work4j.data.exception.SchedulerDataException;
import io.imast.work4j.model.JobDefinition;
import io.imast.work4j.model.JobDefinitionInput;
import io.imast.work4j.model.JobRequestResult;
import io.imast.work4j.model.execution.ExecutionStatus;
import io.imast.work4j.model.execution.ExecutionUpdateInput;
import io.imast.work4j.model.execution.ExecutionIndexEntry;
import io.imast.work4j.model.execution.ExecutionsResponse;
import io.imast.work4j.model.execution.JobExecution;
import io.imast.work4j.model.execution.JobExecutionInput;
import io.imast.work4j.model.iterate.Iteration;
import io.imast.work4j.model.iterate.IterationInput;
import io.imast.work4j.model.iterate.IterationStatus;
import io.imast.work4j.model.iterate.IterationsResponse;
import io.imast.work4j.model.cluster.Worker;
import io.imast.work4j.model.cluster.WorkerHeartbeat;
import io.imast.work4j.model.cluster.WorkerInput;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

/**
 * The scheduler job controller module
 * 
 * @author davitp
 */
@Slf4j
public class SchedulerController  {
    
    /**
     * The scheduler data repository
     */
    protected final SchedulerDataRepository data;
    
    /**
     * Creates new instance of Scheduler Job Controller
     * 
     * @param data The scheduler data repository
     */
    public SchedulerController(SchedulerDataRepository data){
        this.data = data;
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
    public List<JobDefinition> getAllJobs(String tenant, String cluster, String type) throws SchedulerDataException {
        return this.data.getAllJobs(tenant, cluster, type);
    }
    
    /**
     * Gets the job definition by identifier
     * 
     * @param id The job definition id
     * @return Returns job definition if found
     * @throws SchedulerDataException
     */
    public Optional<JobDefinition> getJobById(String id) throws SchedulerDataException {
        return this.data.getJobById(id);
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
    public JobRequestResult getJobPage(String tenant, String cluster, String type, int page, int size) throws SchedulerDataException {
        return this.data.getJobPage(tenant, cluster, type, page, size);
    }
    
    /**
     * Saves a job definition into the data store
     * 
     * @param input The job definition input to save
     * @param replace The optional flag that allows to replace
     * @return Returns saved job definition
     * @throws SchedulerDataException
     */
    public JobDefinition insertJob(JobDefinitionInput input, boolean replace) throws SchedulerDataException {
        return this.data.insertJob(input, replace);
    }
    
    /**
     * Updates an existing job definition 
     * 
     * @param id The job definition id
     * @param input The job definition input to update
     * @return Returns saved job definition
     * @throws SchedulerDataException
     */
    public JobDefinition updateJob(String id, JobDefinitionInput input) throws SchedulerDataException {
        return this.data.updateJob(id, input);
    }
    
    /**
     * Deletes an entry by id and returns deleted one
     * 
     * @param id The id of job definition to delete
     * @return Returns deleted job definition item
     * @throws SchedulerDataException
     */
    public Optional<JobDefinition> deleteJobById(String id) throws SchedulerDataException {
        return this.data.deleteJobById(id);
    }
    
    /**
     * Deletes an entry by location
     * 
     * @param folder The folder of target job
     * @param name The name of job in folder
     * @return Returns deleted job definition item
     * @throws SchedulerDataException
     */
    public Optional<JobDefinition> deleteJobByPath(String folder, String name) throws SchedulerDataException {
        return this.data.deleteJobByPath(folder, name);
    }
    
    /**
     * Deletes all the records
     * 
     * @return Returns number of deleted records
     * @throws SchedulerDataException
     */
    public long deleteAllJobs() throws SchedulerDataException {
        return this.data.deleteAllJobs();
    }
    
    /**
     * Gets all the job executions
     * 
     * @param tenant The tenant to filter
     * @param cluster The optional target cluster to filter
     * @param type The optional type to filter by 
     * @return Returns set of all job executions
     * @throws SchedulerDataException
     */
    public List<JobExecution> getAllExecutions(String tenant, String cluster, String type) throws SchedulerDataException {
        return this.data.getAllExecutions(tenant, cluster, type);
    }
    
    /**
     * Gets all the job executions of job
     * 
     * @param jobId The job id to filter
     * @return Returns set of all job executions
     * @throws SchedulerDataException
     */
    public List<JobExecution> getExecutionsByJob(String jobId) throws SchedulerDataException {
        return this.data.getExecutionsByJob(jobId);
    }
    
    /**
     * Gets all the job executions by given ids
     * 
     * @param ids The set of ids
     * @return Returns set of all job executions
     * @throws SchedulerDataException
     */
    public List<JobExecution> getExecutionsByIds(List<String> ids) throws SchedulerDataException {
        return this.data.getExecutionsByIds(ids);
    }
    
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
    public ExecutionsResponse getExecutionsPage(String tenant, String cluster, String type, int page, int size) throws SchedulerDataException {
        return this.data.getExecutionsPage(tenant, cluster, type, page, size);
    }
    
    /**
     * Gets the set of execution index entries based on query
     * 
     * @param tenant The tenant to filter
     * @param cluster The cluster to filter
     * @return Returns set of execution entries
     */
    public List<ExecutionIndexEntry> getExecutionIndex(String tenant, String cluster) throws SchedulerDataException {
        return this.data.getExecutionIndex(tenant, cluster);
    }
    
    /**
     * Gets the job executions by id
     * 
     * @param id The id of target job execution
     * @return Returns the job execution if found
     * @throws SchedulerDataException
     */
    public Optional<JobExecution> getExecutionById(String id) throws SchedulerDataException {
        return this.data.getExecutionById(id);
    }
    
    /**
     * Inserts new job execution based on input data
     * 
     * @param input The execution input
     * @return Returns created execution instance
     * @throws SchedulerDataException 
     */
    public JobExecution insertJobExecution(JobExecutionInput input) throws SchedulerDataException {
        return this.data.insertJobExecution(input);
    }
    
    /**
     * Updates the execution status of the given job instance
     * 
     * @param id The execution id
     * @param input The execution update input
     * @return Returns updated job execution
     * @throws SchedulerDataException 
     */
    public JobExecution updateExecution(String id, ExecutionUpdateInput input) throws SchedulerDataException {
        return this.data.updateExecution(id, input);
    }
    
    /**
     * Deletes the job execution by id
     * 
     * @param id The id of job execution
     * @return Returns removed job execution if any
     * @throws SchedulerDataException
     */
    public Optional<JobExecution> deleteExecutionById(String id) throws SchedulerDataException {
        return this.data.deleteExecutionById(id);
    }
    
    /**
     * Deletes the executions of the given job id
     * 
     * @param jobId The id of job to filter executions
     * @return Returns number of deleted executions
     * @throws SchedulerDataException
     */
    public long deleteExecutionsByJob(String jobId) throws SchedulerDataException {
       return this.data.deleteExecutionsByJob(jobId);
    }
    
    /**
     * Deletes all the executions by given status codes
     * 
     * @param statuses The target statuses to delete
     * @return Returns number of deleted executions
     * @throws SchedulerDataException
     */
    public long deleteAllExecutionsByStatus(List<ExecutionStatus> statuses) throws SchedulerDataException {
        return this.data.deleteAllExecutionsByStatus(statuses);
    }
    
    /**
     * Deletes all the executions
     * 
     * @return Returns number of deleted executions
     * @throws SchedulerDataException
     */
    public long deleteAllExecutions() throws SchedulerDataException {
        return this.data.deleteAllExecutions();
    }
    
    /**
     * Gets all the job iterations 
     * 
     * @return Returns set of all iterations 
     * @throws SchedulerDataException 
     */
    public List<Iteration> getAllIterations() throws SchedulerDataException {
        return this.data.getAllIterations();
    }
    
    /**
     * Gets all the job iterations for the given job
     * 
     * @param jobId The job id to filter
     * @return Returns set of all job iterations 
     * @throws SchedulerDataException 
     */
    public List<Iteration> getJobIterations(String jobId) throws SchedulerDataException {
        return this.data.getJobIterations(jobId);
    }
    
    /**
     * Gets all the iterations for the given execution
     * 
     * @param executionId The job id to filter
     * @return Returns set of all job iterations for execution
     * @throws SchedulerDataException
     */
    public List<Iteration> getExecutionIterations(String executionId) throws SchedulerDataException {
        return this.data.getExecutionIterations(executionId);
    }
    
    /**
     * Gets the job iteration by identifier
     * 
     * @param id The job iteration id
     * @return Returns job iteration if found
     * @throws SchedulerDataException
     */
    public Optional<Iteration> getIterationById(String id) throws SchedulerDataException {
        return this.data.getIterationById(id);
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
    public IterationsResponse getIterationsPage(String jobId, String executionId, List<IterationStatus> statuses, int page, int size) throws SchedulerDataException {
        return this.data.getIterationsPage(jobId, executionId, statuses, page, size);
    }
    
    /**
     * Inserts a job iteration into the data store
     * 
     * @param input The job iteration to save
     * @return Returns saved job iteration
     * @throws SchedulerDataException
     */
    public Iteration insertIteration(IterationInput input) throws SchedulerDataException {
        return this.data.insertIteration(input);
    }
    
    /**
     * Deletes an entry by id and returns deleted one
     * 
     * @param id The id of job iteration to delete
     * @return Returns deleted job iteration item
     * @throws SchedulerDataException
     */
    public Optional<Iteration> deleteIterationById(String id) throws SchedulerDataException {
        return this.data.deleteIterationById(id);
    }
    
    /**
     * Deletes all the iterations for the given job id
     * 
     * @param jobId The target job id
     * @return Returns number of removed job iteration entries
     * @throws SchedulerDataException
     */
    public long deleteJobIterations(String jobId) throws SchedulerDataException {
        return this.data.deleteJobIterations(jobId);
    }
    
    /**
     * Deletes all the iterations for the given execution id
     * 
     * @param executionId The target execution id
     * @return Returns number of removed execution iteration entries
     * @throws SchedulerDataException
     */
    public long deleteExecutionIterations(String executionId) throws SchedulerDataException {
        return this.data.deleteExecutionIterations(executionId);
    }
    
    /**
     * Deletes all the iterations
     * 
     * @return Returns number of deleted records
     * @throws SchedulerDataException
     */
    public long deleteAllIterations() throws SchedulerDataException {
        return this.data.deleteAllIterations();
    }
    
    /**
     * Deletes all the iterations before given timestamp
     * 
     * @param timestamp The timestamp to filter
     * @return Returns number of deleted items
     * @throws SchedulerDataException
     */
    public long deleteIterationsBefore(Date timestamp) throws SchedulerDataException {
       return this.data.deleteIterationsBefore(timestamp);
    }
    
    /**
     * Gets all the workers
     * 
     * @return Returns set of all workers
     * @throws SchedulerDataException
     */
    public List<Worker> getAllWorkers() throws SchedulerDataException {
        return this.data.getAllWorkers();
    }
    
    /**
     * Gets the set of workers within a cluster
     * 
     * @param cluster The cluster to filter
     * @return Returns set of cluster workers
     * @throws SchedulerDataException
     */
    public List<Worker> getAllWorkers(String cluster) throws SchedulerDataException {
       return this.data.getAllWorkers(cluster);
    }
    
    /**
     * Gets the worker by identifier
     * 
     * @param id The agent definition id
     * @return Returns agent definition if found
     * @throws SchedulerDataException
     */
    public Optional<Worker> getWorkerById(String id) throws SchedulerDataException {
        return this.data.getWorkerById(id);
    }
    
    /**
     * Inserts a worker into the data store
     * 
     * @param input The worker input
     * @return Returns saved worker
     * @throws SchedulerDataException
     */
    public Worker insertWorker(WorkerInput input) throws SchedulerDataException {
        return this.data.insertWorker(input);
    }
    
    /**
     * Updates a worker in the data store
     * 
     * @param id The id of worker session
     * @param heartbeat The heartbeat to update
     * @return Returns saved worker 
     * @throws SchedulerDataException
     */
    public Worker updateWorker(String id, WorkerHeartbeat heartbeat) throws SchedulerDataException {
        return this.data.updateWorker(id, heartbeat);
    }
    
    /**
     * Deletes all the idle workers for the given cluster and machine
     * 
     * @param cluster The cluster to filter
     * @param name The name of machine to remove
     * @return Returns number of deleted sessions
     * @throws SchedulerDataException
     */
    public long deleteIdleWorkers(String cluster, String name) throws SchedulerDataException {
        return this.data.deleteIdleWorkers(cluster, name);
    }
    
    /**
     * Deletes all the workers for the given cluster and machine
     * 
     * @param cluster The cluster to filter
     * @param name The name of machine to remove
     * @return Returns number of deleted sessions
     * @throws SchedulerDataException
     */
    public long deleteWorkers(String cluster, String name) throws SchedulerDataException {
        return this.data.deleteWorkers(cluster, name);
    }
       
    /**
     * Deletes an entry by id and returns deleted one
     * 
     * @param id The id of worker to delete
     * @return Returns deleted worker item
     * @throws SchedulerDataException
     */
    public Optional<Worker> deleteWorkerById(String id) throws SchedulerDataException {
        return this.data.getWorkerById(id);
    }
}
