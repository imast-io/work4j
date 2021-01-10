package io.imast.work4j.data;

import io.imast.work4j.model.JobDefinition;
import io.imast.work4j.model.JobRequestResult;
import io.imast.work4j.model.JobStatus;
import java.util.List;
import java.util.Optional;

/**
 * The abstract repository for the job definition
 * 
 * @author davitp
 */
public interface JobDefinitionRepository  {
    
    /**
     * A special procedure to prepare schema
     * 
     * @return Returns if schema is ready
     */
    public boolean prepare();
    
    /**
     * Gets the job definition by identifier
     * 
     * @param id The job definition id
     * @return Returns job definition if found
     */
    public Optional<JobDefinition> getById(String id);
    
    /**
     * Gets all the job definitions
     * 
     * @return Returns set of all job definitions
     */
    public List<JobDefinition> getAll();
    
    /**
     * Get the page of job definitions sorted by code
     * 
     * @param page The page number
     * @param size The page size
     * @return Returns a page of job definitions
     */
    public JobRequestResult<JobDefinition> getPageByCode(int page, int size);
            
    /**
     * Find all jobs by type
     * 
     * @param type The type of job
     * @return Returns set of jobs
     */
    public List<JobDefinition> getByType(String type);
    
    /**
     * Gets all entries by agent and status
     * 
     * @param type The type of job
     * @param group The group to filter
     * @param cluster The cluster
     * @param statuses The status
     * @return Returns filtered jobs
     */
    public List<JobDefinition> getByStatusIn(String type, String group, String cluster, List<JobStatus> statuses);
    
    /**
     * Gets all entries by agent and status not in
     * 
     * @param type The type of job
     * @param group The group to filter
     * @param cluster The cluster
     * @param statuses The status
     * @return Returns filtered jobs
     */
    public List<JobDefinition> getByStatusNotIn(String type, String group, String cluster, List<JobStatus> statuses);
    
    /**
     * Gets all groups
     * 
     * @param cluster The target cluster
     * @return Returns all the groups in the system
     */
    public List<String> getAllGroups(String cluster);
    
    /**
     * Gets all types available
     * 
     * @param cluster The target cluster
     * @return Returns all the types
     */
    public List<String> getAllTypes(String cluster);
    
    /**
     * Saves (inserts or updates) a job definition into the data store
     * 
     * @param jobDefinition The job definition to save
     * @return Returns saved job definition
     */
    public Optional<JobDefinition> update(JobDefinition jobDefinition);
    
    /**
     * Saves a job definition into the data store
     * 
     * @param jobDefinition The job definition to save
     * @return Returns saved job definition
     */
    public Optional<JobDefinition> insert(JobDefinition jobDefinition);
    
    /**
     * Deletes an entry by id and returns deleted one
     * 
     * @param id The id of job definition to delete
     * @return Returns deleted job definition item
     */
    public Optional<JobDefinition> deleteById(String id);
    
    /**
     * Deletes all the records
     * 
     * @return Returns number of deleted records
     */
    public long deleteAll();
}
