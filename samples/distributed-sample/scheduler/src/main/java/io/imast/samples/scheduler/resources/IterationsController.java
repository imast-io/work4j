package io.imast.samples.scheduler.resources;

import io.imast.work4j.controller.SchedulerController;
import io.imast.work4j.model.iterate.*;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * The Scheduler Jobs Controller
 * 
 * @author davitp
 */
@RestController
@RequestMapping("/api/v1/scheduler/iterations")
public class IterationsController {
    
    /**
     * The job scheduler controller
     */
    @Autowired
    private SchedulerController schedulerController;
    
    /**
     * Get all iterations
     * 
     * @return Returns found job iterations
     */
    @GetMapping(path = "", params = { })
    public ResponseEntity<?> getIterations(){
        return ResponseEntity.ok(this.schedulerController.getAllIterations());
    }
    
    /**
     * Get all iterations by job id
     * 
     * @param jobId The job id
     * @return Returns found job iterations
     */
    @GetMapping(path = "", params = { "jobId" })
    public ResponseEntity<?> getIterationsByJob(@RequestParam String jobId){
        return ResponseEntity.ok(this.schedulerController.getJobIterations(jobId));
    }
    
    /**
     * Get all iterations by execution id
     * 
     * @param executionId The execution id
     * @return Returns found job iterations
     */
    @GetMapping(path = "", params = { "executionId" })
    public ResponseEntity<?> getIterationsByExecution(@RequestParam String executionId){
        return ResponseEntity.ok(this.schedulerController.getExecutionIterations(executionId));
    }
    
    /**
     * Get iterations page of execution by its id
     * 
     * @param executionId The ID of requested job
     * @param statuses The iteration statuses
     * @param page The page number
     * @param size The page size
     * @return Returns found job iterations
     */
    @GetMapping(path = "", params = { "executionId", "statuses", "page", "size" })
    public ResponseEntity<?> getIterationsPageByExecution(@RequestParam String executionId, List<IterationStatus> statuses, @RequestParam int page, @RequestParam int size){
        return ResponseEntity.ok(this.schedulerController.getIterationsPage(null, executionId, statuses, page, size));
    }
    
    /**
     * Get iterations page
     * 
     * @param statuses The iteration statuses
     * @param page The page number
     * @param size The page size
     * @return Returns found job iterations
     */
    @GetMapping(path = "", params = {"statuses", "page", "size" })
    public ResponseEntity<?> getIterationsPage(List<IterationStatus> statuses, @RequestParam int page, @RequestParam int size){
        return ResponseEntity.ok(this.schedulerController.getIterationsPage(null, null, statuses, page, size));
    }
    
    /**
     * Find the iteration by id
     * 
     * @param id The ID of requested iteration
     * @return Returns found iteration
     */
    @GetMapping(path = "{id}")
    public ResponseEntity<?> getById(@PathVariable String id){
        return ResponseEntity.of(this.schedulerController.getIterationById(id));
    }
    
    /**
     * Create an iteration of job 
     * 
     * @param iteration The job iteration
     * @return Returns saved iteration
     */
    @PostMapping(path = "")
    public ResponseEntity<?> postIteration(@RequestBody IterationInput iteration){
        return ResponseEntity.ok(this.schedulerController.insertIteration(iteration));
    }
    
    /**
     * Delete iteration from scheduler
     * 
     * @param id The iteration id to remove
     * @return Returns removed iteration
     */
    @DeleteMapping(path = "{id}")
    public ResponseEntity<?> deleteById(@PathVariable String id){
        return ResponseEntity.of(this.schedulerController.deleteIterationById(id));
    }
    
    /**
     * Delete all iterations from scheduler by job id
     * 
     * @param jobId The job identifier
     * @return Returns number of deleted items
     */
    @DeleteMapping(path = "", params = { "jobId"})
    public ResponseEntity<?> deleteByJob(@RequestParam String jobId){
        return ResponseEntity.ok(this.schedulerController.deleteJobIterations(jobId));
    }
    
    /**
     * Delete all iterations from scheduler by execution id
     * 
     * @param executionId The execution identifier
     * @return Returns number of deleted items
     */
    @DeleteMapping(path = "", params = { "executionId"})
    public ResponseEntity<?> deleteByExecution(@RequestParam String executionId){
        return ResponseEntity.ok(this.schedulerController.deleteExecutionIterations(executionId));
    }
    
    /**
     * Delete all the iterations from scheduler
     * 
     * @param all The safety flag to confirm
     * @return Returns number of delete items
     */
    @DeleteMapping(path = "", params = { "all"})
    public ResponseEntity<?> deleteAll(@RequestParam(required = true) boolean all){
        return ResponseEntity.ok(this.schedulerController.deleteAllIterations());
    }
}
