package io.imast.samples.scheduler.resources;

import java.util.List;
import io.imast.work4j.controller.SchedulerController;
import io.imast.work4j.model.execution.ExecutionUpdateInput;
import io.imast.work4j.model.execution.JobExecutionInput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * The remote controller for executions
 * 
 * @author davitp
 */
@RestController
@RequestMapping("/api/v1/scheduler/executions")
public class ExecutionsController {
    
    /**
     * The job scheduler controller
     */
    @Autowired
    private SchedulerController schedulerController;
    
    /**
     * Gets all the executions in the system
     * 
     * @param cluster The target cluster 
     * @param type The optional type parameter
     * @return Returns set of executions
     */
    @GetMapping(path = "")
    public ResponseEntity<?> getAll(@RequestParam(required = false) String cluster, @RequestParam(required = false) String type){
        return ResponseEntity.ok(this.schedulerController.getAllExecutions(cluster, type));
    }
        
    /**
     * Find the execution by id
     * 
     * @param id The ID of requested execution
     * @return Returns found execution
     */
    @GetMapping(path = "{id}")
    public ResponseEntity<?> getById(@PathVariable String id){
        return ResponseEntity.of(this.schedulerController.getExecutionById(id));
    }
    
    /**
     * Find the executions by given ids
     * 
     * @param ids The ids of requested executions
     * @return Returns found executions
     */
    @GetMapping(path = "", params = { "ids" })
    public ResponseEntity<?> getByIds(@RequestParam List<String> ids){
        return ResponseEntity.ok(this.schedulerController.getExecutionsByIds(ids));
    }
    
    /**
     * Gets all executions by page 
     * 
     * @param cluster The target cluster 
     * @param type The optional type parameter 
     * @param page The page number
     * @param size The page size
     * @return Returns set of job executions
     */
    @GetMapping(path = "", params = {"page", "size"})
    public ResponseEntity<?> getPage(@RequestParam(required = false) String cluster, @RequestParam(required = false) String type, @RequestParam Integer page, @RequestParam Integer size){
        return ResponseEntity.ok(this.schedulerController.getExecutionsPage(cluster, type, page, size));
    }
    
    /**
     * Gets all executions index 
     * 
     * @param cluster The target cluster 
     * @return Returns job execution index
     */
    @GetMapping(path = "", params = {"cluster"})
    public ResponseEntity<?> getExecutionIndex(@RequestParam(required = true) String cluster){
        return ResponseEntity.ok(this.schedulerController.getExecutionIndex(cluster));
    }
    
    /**
     * Add a job execution to controller
     * 
     * @param execution The job execution to submit
     * @return Returns added entity
     */
    @PostMapping(path = "")
    public ResponseEntity<?> postOne(@RequestBody JobExecutionInput execution){
        return ResponseEntity.ok(this.schedulerController.insertJobExecution(execution));
    }
    
    /**
     * Update a job execution to controller
     * 
     * @param id The ID of job execution to update
     * @param input The job execution update to add
     * @return Returns updated entity
     */
    @PutMapping(path = "{id}")
    public ResponseEntity<?> updateOne(@PathVariable String id, @RequestBody ExecutionUpdateInput input){
        return ResponseEntity.ok(this.schedulerController.updateExecution(id, input));
    }
    
    /**
     * Delete execution from scheduler
     * 
     * @param id The execution id to remove
     * @return Returns removed execution
     */
    @DeleteMapping(path = "{id}")
    public ResponseEntity<?> delete(@PathVariable String id){
        return ResponseEntity.of(this.schedulerController.deleteExecutionById(id));
    }
    
    /**
     * Delete executions of given job
     * 
     * @param jobId The executions by job identifier
     * @return Returns removed job
     */
    @DeleteMapping(path = "", params = { "jobId"})
    public ResponseEntity<?> deleteAllByJob(@RequestParam(required = true) String jobId){
        return ResponseEntity.ok(this.schedulerController.deleteExecutionsByJob(jobId));
    }
    
    /**
     * Delete all the executions from scheduler
     * 
     * @param all The safety flag to confirm
     * @return Returns removed job
     */
    @DeleteMapping(path = "", params = { "all"})
    public ResponseEntity<?> deleteAll(@RequestParam(required = true) boolean all){
        return ResponseEntity.ok(this.schedulerController.deleteAllExecutions());
    }
}
