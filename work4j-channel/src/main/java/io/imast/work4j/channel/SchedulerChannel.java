package io.imast.work4j.channel;

import java.util.Optional;
import io.imast.work4j.model.*;
import io.imast.work4j.model.agent.*;
import io.imast.work4j.model.iterate.*;
import io.imast.work4j.model.exchange.*;

/**
 * The worker channel interface 
 * 
 * @author davitp
 */
public interface SchedulerChannel {
    
    /**
     * Pull job groups for the given cluster
     * 
     * @param request The job group request filter
     * @return Returns job group identities
     */
    public Optional<JobMetadataResponse> metadata(JobMetadataRequest request);
    
    /**
     * Exchange current status with modified entries
     * 
     * @param status The status exchange structure
     * @return Returns modified entries
     */
    public Optional<JobStatusExchangeResponse> statusExchange(JobStatusExchangeRequest status);
    
    /**
     * Adds iteration information to scheduler
     * 
     * @param iteration The iteration to register
     * @return Returns registered iteration
     */
    public Optional<JobIteration> iterate(JobIteration iteration);
    
    /**
     * Sets the status of job definition 
     * 
     * @param id The identifier of job definition
     * @param status The new status of job
     * @return Returns updated job definition
     */
    public Optional<JobDefinition> markAs(String id, JobStatus status);
    
    /**
     * Registers agent definition into the system 
     * 
     * @param agent The agent definition to register
     * @return Returns registered agent definition
     */
    public Optional<AgentDefinition> registration(AgentDefinition agent);
    
    /**
     * Send a Heartbeat signal to scheduler
     * 
     * @param id The identifier of agent definition
     * @param health The health status of agent
     * @return Returns updated agent definition
     */
    public Optional<AgentDefinition> heartbeat(String id, AgentHealth health);
}
