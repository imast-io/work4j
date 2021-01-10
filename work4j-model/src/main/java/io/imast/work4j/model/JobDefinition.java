package io.imast.work4j.model;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The Job Definition model
 * 
 * @author davitp
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class JobDefinition implements Serializable {
    
    /**
     * The entry ID
     */
    private String id;
    
    /**
     * The job code
     */
    private String code;
    
    /**
     * The job group
     */
    private String group;
    
    /**
     * The job type
     */
    private String type;
    
    /**
     * The type of schedule
     */
    private JobScheduleType scheduleType;
    
    /**
     * The static period to use in milliseconds
     */
    private Double period;
    
    /**
     * The end time for job
     */
    private ZonedDateTime endAt;
    
    /**
     * The Cron schedule of request
     */
    private List<CronTrigger> cronTriggers;
    
    /**
     * The status of job
     */
    private JobStatus status;
    
    /**
     * The cluster for the job
     */
    private String cluster;
    
    /**
     * The execution options
     */
    private JobExecutionOptions execution;
    
    /**
     * The set of selectors
     */
    private Map<String, String> selectors;
    
    /**
     * The Job Data
     */
    private JobData jobData;
    
    /**
     * The job timezone
     */
    private String timezone;
    
    /**
     * The user that defined the job
     */
    private String createdBy;
    
    /**
     * The user that modified the job
     */
    private String modifiedBy;
    
    /**
     * Timestamp of creation
     */
    private ZonedDateTime created;
    
    /**
     * The time of last modification
     */
    private ZonedDateTime modified;
    
    /**
     * The extra information required for execution
     */
    private Map<String, Object> extra;
}
