package io.imast.work4j.worker.instance;

import java.util.Objects;

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
    
    /**
     * Checks if two key objects are equal
     * 
     * @param other The other to compare
     * @return Returns true if considering equal
     */
    @Override
    public boolean equals(Object other){
        
        // not equal if other type or null
        if(other == null || !(other instanceof ExecutionKey)){
            return false;
        }
        
        // cast to key type
        var otherKey = (ExecutionKey) other;
        
        // returns true if both fields are equal
        return Objects.equals(this.executionId, otherKey.executionId) && Objects.equals(this.jobId, otherKey.jobId);
    }

    /**
     * Generate the hash code for the key
     * 
     * @return Returns the key hash code
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.executionId);
        hash = 37 * hash + Objects.hashCode(this.jobId);
        return hash;
    }
    
    /**
     * Parse the execution key string into execution key object
     * 
     * @param executionKey The execution key string
     * @return Returns execution key object
     */
    public static ExecutionKey from(String executionKey){
        
        // split into two parts if possible 
        var split = executionKey.split(executionKey, 2);
        
        // get the execution id
        var executionId = split.length > 0 ? split[0] : null;
        
        // get the job id if available
        var jobId = split.length > 1 ? split[1] : null;
        
        return new ExecutionKey(executionId, jobId);
    }
}
