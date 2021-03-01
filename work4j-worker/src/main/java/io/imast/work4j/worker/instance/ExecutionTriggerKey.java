package io.imast.work4j.worker.instance;

/**
 * The single execution trigger key
 * 
 * @author davitp
 */
public class ExecutionTriggerKey {
    
    /**
     * The trigger name
     */
    private final String triggerName;
    
    /**
     * The string representation of execution key
     */
    private final String executionKey;
        
    /**
     * Creates new instance of execution trigger key
     * 
     * @param triggerName The execution id
     * @param executionKey The string representation of execution key
     */
    public ExecutionTriggerKey(String triggerName, String executionKey){
        this.triggerName = triggerName;
        this.executionKey = executionKey;
    }
    
    /**
     * Gets the trigger name
     * 
     * @return Returns trigger name
     */
    public String getTriggerName(){
        return this.triggerName;
    }
    
    /**
     * Gets the execution key
     * 
     * @return Returns the execution key
     */
    public String getExecutionKey(){
        return this.executionKey;
    }
}
