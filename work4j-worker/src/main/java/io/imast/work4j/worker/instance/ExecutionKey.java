package io.imast.work4j.worker.instance;

/**
 * The single execution key
 * 
 * @author davitp
 */
public class ExecutionKey {
    
    /**
     * The execution identifier
     */
    private final String executionId;
    
    /**
     * The job identifier
     */
    private final String jobId;
    
    /**
     * Creates new instance of execution key
     * 
     * @param executionId The execution id
     * @param jobId The job id
     */
    public ExecutionKey(String executionId, String jobId){
        this.executionId = executionId;
        this.jobId = jobId;
    }
    
    /**
     * Gets the execution id
     * 
     * @return Returns execution id
     */
    public String getExecutionId(){
        return this.executionId;
    }
    
    /**
     * Gets the job id
     * 
     * @return Returns the job id
     */
    public String getJobId(){
        return this.jobId;
    }
    
    /**
     * Gets the string representation of execution key
     * 
     * @return Returns the string representation of execution key
     */
    @Override
    public String toString(){
        return String.format("%s:%s", this.executionId, this.jobId);
    }
}
