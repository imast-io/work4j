package io.imast.work4j.worker;

/**
 * The definitions of job management constants
 * 
 * @author davitp
 */
public class JobConstants {
   
    /**
     * The default cluster name
     */
    public static final String DEFAULT_CLUSTER = "/";
    
    /**
     * The job session context
     */
    public static final String JOB_SESSION_CONTEXT = "SESSION";
        
    /**
     * The job execution id key
     */
    public static final String PAYLOAD_JOB_EXECUTION_ID = "_PLD_JOB_EXECUTION_ID";
    
    /**
     * The job id key
     */
    public static final String PAYLOAD_JOB_DEFINITION_ID = "_PLD_JOB_DEFINITION_ID";
  
    /**
     * The job name key
     */
    public static final String PAYLOAD_JOB_NAME = "_PLD_JOB_NAME";
    
    /**
     * The job folder key
     */
    public static final String PAYLOAD_JOB_FOLDER = "_PLD_JOB_FOLDER";
    
    /**
     * The job type key
     */
    public static final String PAYLOAD_JOB_TYPE = "_PLD_JOB_TYPE";
    
    /**
     * The job cluster key
     */
    public static final String PAYLOAD_JOB_CLUSTER = "_PLD_JOB_CLUSTER";
    
    /**
     * The job execution options key
     */
    public static final String PAYLOAD_JOB_OPTIONS = "_PLD_JOB_OPTIONS";
        
    /**
     * The job modules
     */
    public static final String JOB_MODULES = "JOB_MODULES";
    
    /**
     * The job factory
     */
    public static final String WORKER_FACTORY = "WORKER_FACTORY";
}
