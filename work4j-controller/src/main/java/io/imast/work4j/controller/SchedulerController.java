package io.imast.work4j.controller;

import io.imast.core.Str;
import io.imast.core.Zdt;
import io.imast.work4j.channel.SchedulerChannel;
import io.imast.work4j.channel.UpdateOperation;
import io.imast.work4j.channel.WorkerUpdateMessage;
import io.imast.work4j.channel.notify.WorkerPublisher;
import io.imast.work4j.data.AgentDefinitionRepository;
import io.imast.work4j.data.JobDefinitionRepository;
import io.imast.work4j.data.JobIterationRepository;
import io.imast.work4j.model.JobDefinition;
import io.imast.work4j.model.JobRequestResult;
import io.imast.work4j.model.JobStatus;
import io.imast.work4j.model.agent.AgentDefinition;
import io.imast.work4j.model.agent.AgentHealth;
import io.imast.work4j.model.exchange.JobMetadataRequest;
import io.imast.work4j.model.exchange.JobMetadataResponse;
import io.imast.work4j.model.exchange.JobStatusExchangeRequest;
import io.imast.work4j.model.exchange.JobStatusExchangeResponse;
import io.imast.work4j.model.iterate.IterationStatus;
import io.imast.work4j.model.iterate.JobIteration;
import io.imast.work4j.model.iterate.JobIterationsResult;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/**
 * The scheduler job controller module
 * 
 * @author davitp
 */
@Slf4j
public class SchedulerController implements SchedulerChannel {

    /**
     * The job definition repository
     */
    protected final JobDefinitionRepository definitions;
    
    /**
     * The job iterations repository
     */
    protected final JobIterationRepository iterations;
     
    /**
     * The agent definitions repository
     */
    protected final AgentDefinitionRepository agents;
    
    /**
     * The set of worker publishers
     */
    protected final List<WorkerPublisher> workerPublishers;
   
    /**
     * Creates new instance of Scheduler Job Controller
     * 
     * @param definitions The job definition repository
     * @param iterations The job iterations repository
     * @param agents The agent definition repository
     * @param workerPublishers The set of worker publishers
     */
    public SchedulerController(JobDefinitionRepository definitions, JobIterationRepository iterations, AgentDefinitionRepository agents, List<WorkerPublisher> workerPublishers){
        this.definitions = definitions;
        this.iterations = iterations;
        this.agents = agents;
        this.workerPublishers = new ArrayList<>(workerPublishers);
    }
    
    /**
     * Initializes job scheduler controller before usage
     * 
     * @return Returns initialization result
     */
    public SchedulerController initialize(){
        
        // prepare agents
        this.agents.prepare();
        
        // prepare job definitions
        this.definitions.prepare();
        
        // prepare job iterations;
        this.iterations.prepare();
        
        return this;
    }
    
    /**
     * Gets metadata for the scheduler
     * 
     * @param request The request of metadata
     * @return Returns metadata response
     */
    @Override
    public Optional<JobMetadataResponse> metadata(JobMetadataRequest request){
        
        // get queried cluster
        var cluster = request.getCluster();
        
        // get groups in cluster
        var groups = this.definitions.getAllGroups(cluster);
        
        // get types in cluster
        var types = this.definitions.getAllTypes(cluster);
        
        return Optional.of(new JobMetadataResponse(cluster, groups, types));
    }
    
    /**
     * Calculate changes relative to the status
     * 
     * @param status The current status
     * @return Returns change set
     */
    @Override
    public Optional<JobStatusExchangeResponse> statusExchange(JobStatusExchangeRequest status){
        
        // get all the jobs for that type
        List<JobDefinition> all = this.getAllActive(status.getGroup(), status.getType(), status.getCluster()).getJobs();
        
        // all job keys
        Set<String> allKeys = all.stream().map(j -> j.getCode()).collect(Collectors.toSet());
        
        // all the new jobs 
        HashMap<String, JobDefinition> newJobs = new HashMap<>();
        
        // all the updated jobs 
        HashMap<String, JobDefinition> updatedJobs = new HashMap<>();
        
        // jobs that were not modified
        HashMap<String, JobDefinition> nonModified = new HashMap<>();
        
        // the IDs of deleted jobs
        List<String> deleted = new ArrayList<>();
                
        // process all jobs to compute changes
        for(JobDefinition job : all){
            
            // get last update time of job captured by requester
            var existing = status.getState().getOrDefault(job.getCode(), null);
                        
            // check if does not in current state
            if(existing == null){
                newJobs.put(job.getCode(), job);
                continue;
            }
            
            // if not modified
            if(Zdt.sameTime(job.getModified(), existing)){
                nonModified.put(job.getCode(), job);
                continue;
            }
            
            // if job is fresher add ot update list
            if(job.getModified().isAfter(existing)){
                updatedJobs.put(job.getCode(), job);
            }            
        }
        
        // traverse current jobs to see deleted items
        status.getState().keySet().forEach(statusJob -> {
            if(!allKeys.contains(statusJob)){
                deleted.add(statusJob);
            }
        });
     
        return Optional.of(new JobStatusExchangeResponse(status.getGroup(), status.getType(), deleted, updatedJobs, newJobs));
    }
    
    /**
     * Adds job iteration record
     * 
     * @param iteration The job iteration to store
     * @return Returns saved job iteration
     */
    @Override
    public Optional<JobIteration> iterate(JobIteration iteration){
        return this.iterations.insert(iteration);
    }
    
    /**
     * Cleanup iterations
     * 
     * @param upper The upper bound to cleanup
     * @return Returns number of deleted records
     */
    public long cleanupIterations(ZonedDateTime upper){
        return this.iterations.deleteBefore(upper);
    }
    
    /**
     * Mark job with given status
     * 
     * @param id The id of target job
     * @param status The new status to assign
     * @return Returns modified job definition if any
     */
    @Override
    public Optional<JobDefinition> markAs(String id, JobStatus status){
        
        // get job by id
        var existing = this.getJob(id);
        
        // does not exist
        if(!existing.isPresent()){
            return Optional.empty();
        }
        
        // update status
        return this.updateJob(existing.get().toBuilder().status(status).build());
    }
    
    /**
     * Adds agent definition 
     * 
     * @param agent The agent to register
     * @return Returns registered agent 
     */
    @Override
    public Optional<AgentDefinition> registration(AgentDefinition agent){
        return this.agents.update(agent);
    }
    
    /**
     * Heartbit registration with new health info
     * 
     * @param id The id of agent definition
     * @param health The health of agent
     * @return Returns saved agent definition
     */
    @Override
    public Optional<AgentDefinition> heartbeat(String id, AgentHealth health){
        
        // check agent if exists
        var existing = this.getAgent(id);
        
        // nothing to update
        if(!existing.isPresent()){
            return Optional.empty();
        }
        
        // update health and store
        return this.agents.update(existing.get().toBuilder().health(health).build());
    }
    
    /**
     * Gets all job definitions
     * 
     * @param type The type to filter with
     * @return Returns all jobs filtered by type
     */
    public List<JobDefinition> getAllJobs(String type){
        return Str.blank(type) ? this.definitions.getAll() : this.definitions.getByType(type);
    }
    
    /**
     * Adds a job definition 
     * 
     * @param definition The job definition to add
     * @return Returns job definition
     */
    public Optional<JobDefinition> addJob(JobDefinition definition){
        // insert job definition
        var inserted = this.definitions.insert(definition);
        
        // publish if needed
        if(inserted.isPresent()){
            this.publishWorkerUpdate(new WorkerUpdateMessage(UpdateOperation.ADD, inserted.get().getCode(), inserted.get().getGroup(), inserted.get()));
        }
        
        return inserted;
    }
    
    /**
     * Updates an existing job definition 
     * 
     * @param definition The job definition to update
     * @return Returns job definition
     */
    public Optional<JobDefinition> updateJob(JobDefinition definition){ 
        
        // get existing definition
        var existing = this.getJob(definition.getId());
        
        // nothing to update
        if(!existing.isPresent()){
            return Optional.empty();
        }
        
        // update job definition
        var updated = this.definitions.update(definition);
        
        // nothing to do more
        if(!updated.isPresent()){
            return updated;
        }
        
        // the previous status
        var prevStatus = existing.get().getStatus();
        
        // the new status
        var newStatus = updated.get().getStatus();
        
        // if was inactive and remains inactive do nothing
        if(prevStatus != JobStatus.ACTIVE && newStatus != JobStatus.ACTIVE){
            return updated;
        }
        
        // if remains active
        if(prevStatus == JobStatus.ACTIVE && newStatus == JobStatus.ACTIVE){
            this.publishWorkerUpdate(new WorkerUpdateMessage(UpdateOperation.UPDATE, updated.get().getCode(), updated.get().getGroup(), updated.get()));
        }
        
        // if becomes inactive
        if(prevStatus == JobStatus.ACTIVE && newStatus != JobStatus.ACTIVE){
            this.publishWorkerUpdate(new WorkerUpdateMessage(UpdateOperation.REMOVE, updated.get().getCode(), updated.get().getGroup(), null));
        }
        
        // if becomes active
        if(prevStatus == JobStatus.ACTIVE && newStatus != JobStatus.ACTIVE){
            this.publishWorkerUpdate(new WorkerUpdateMessage(UpdateOperation.ADD, updated.get().getCode(), updated.get().getGroup(), updated.get()));
        }
        
        return updated;
    }
    
    /**
     * Get the job by id
     * 
     * @param id The ID of requested job
     * @return Returns found job
     */
    public Optional<JobDefinition> getJob(String id){
        return this.definitions.getById(id);
    }
    
    /**
     * Deletes job by id
     * 
     * @param id The job id to delete
     * @return Returns deleted job definition
     */
    public Optional<JobDefinition> deleteJob(String id){
        var deleted = this.definitions.deleteById(id);
        
        if(!deleted.isPresent()){
            return deleted;
        }
        
        this.publishWorkerUpdate(new WorkerUpdateMessage(UpdateOperation.REMOVE, deleted.get().getCode(), deleted.get().getGroup(), null));    
        
        return deleted;
    }
    
    /**
     * Gets jobs definition page
     * 
     * @param page The page number
     * @param size The page size
     * @return Return the job definition page
     */
    public JobRequestResult getJobsPage(int page, int size){
        return this.definitions.getPageByCode(page, size);
    }
    
    /**
     * Gets the page of iterations ordered by timestamp (optionally filter by job id and statuses)
     * 
     * @param jobId The job id to filter iterations
     * @param status The set of target status to lookup
     * @param page The page number
     * @param size The page size
     * @return Returns a page of iterations with given filter
     */
    public JobIterationsResult<JobIteration> getIterations(String jobId, IterationStatus status, int page, int size){
        return this.iterations.getPageByTimestamp(jobId, status == null ? null : Arrays.asList(status), page, size);
    }
    
    /**
     * Gets all active jobs for given type and agent
     * This will exclude "defined", "paused", "completed" and "failed" jobs
     * 
     * @param group The group to filter
     * @param type The type to check
     * @param cluster The agent 
     * @return Returns incomplete jobs
     */
    protected JobRequestResult getAllActive(String group, String type, String cluster){
        
        // get jobs by cluster and status
        var jobs = this.definitions.getByStatusIn(type, group, cluster, Arrays.asList(JobStatus.ACTIVE));
        
        return new JobRequestResult(jobs, (long)jobs.size());
    }
    
    /**
     * Gets all agents
     * 
     * @return Returns agent definitions
     */
    public List<AgentDefinition> getAgents(){
        return this.agents.getAll();
    }
    
    /**
     * Gets the agent by identifier
     * 
     * @param id The agent definition id
     * @return Returns agent definition by identifier
     */
    public Optional<AgentDefinition> getAgent(String id){
        return this.agents.getById(id);
    }
    
    /**
     * Deletes agent definition 
     * 
     * @param id The agent id to delete
     * @return Returns deleted agent 
     */
    public Optional<AgentDefinition> deleteAgent(String id){
        return this.agents.deleteById(id);
    }
    
    /**
     * Publishes worker update
     * 
     * @param message The message to publish
     */
    protected void publishWorkerUpdate(WorkerUpdateMessage message){
        this.workerPublishers.forEach(pub -> pub.publish(message));
    }
}
