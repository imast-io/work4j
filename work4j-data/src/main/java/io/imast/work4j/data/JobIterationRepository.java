package io.imast.work4j.data;

import io.imast.work4j.model.iterate.IterationStatus;
import io.imast.work4j.model.iterate.JobIteration;
import io.imast.work4j.model.iterate.JobIterationsResult;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

/**
 * The job iteration repository
 * 
 * @author davitp
 */
public interface JobIterationRepository {
    
    /**
     * A special procedure to prepare schema
     * 
     * @return Returns if schema is ready
     */
    public boolean prepare();
    
    /**
     * Gets the job iteration by identifier
     * 
     * @param id The job iteration id
     * @return Returns job iteration if found
     */
    public Optional<JobIteration> getById(String id);
    
    /**
     * Gets all the job iterations for the given job
     * 
     * @param jobId The job id to filter
     * @return Returns set of all job iterations 
     */
    public List<JobIteration> getAll(String jobId);
    
    /**
     * Gets the page of iterations ordered by timestamp (optionally filter by job id and statuses)
     * 
     * @param jobId The job id to filter iterations
     * @param statuses The set of target statuses to lookup
     * @param page The page number
     * @param size The page size
     * @return Returns a page of iterations with given filter
     */
    public JobIterationsResult getPageByTimestamp(String jobId, List<IterationStatus> statuses, int page, int size);
    
    /**
     * Updates (or inserts) a job iteration into the data store
     * 
     * @param jobIteration The job iteration to save
     * @return Returns saved job iteration
     */
    public Optional<JobIteration> update(JobIteration jobIteration);
    
    /**
     * Inserts a job iteration into the data store
     * 
     * @param jobIteration The job iteration to save
     * @return Returns saved job iteration
     */
    public Optional<JobIteration> insert(JobIteration jobIteration);
    
    /**
     * Deletes an entry by id and returns deleted one
     * 
     * @param id The id of job iteration to delete
     * @return Returns deleted job iteration item
     */
    public Optional<JobIteration> deleteById(String id);
    
    /**
     * Deletes all the records
     * 
     * @return Returns number of deleted records
     */
    public long deleteAll();
    
    /**
     * Deletes all the entries before given timestamp
     * 
     * @param timestamp The timestamp to filter
     * @return Returns number of deleted items
     */
    public long deleteBefore(ZonedDateTime timestamp);
}
