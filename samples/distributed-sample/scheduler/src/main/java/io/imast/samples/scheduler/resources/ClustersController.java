package io.imast.samples.scheduler.resources;

import io.imast.work4j.controller.SchedulerController;
import io.imast.work4j.model.cluster.WorkerHeartbeat;
import io.imast.work4j.model.cluster.WorkerJoinInput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The workers controller
 * 
 * @author davitp
 */
@RestController
@RequestMapping("/api/v1/scheduler/clusters")
public class ClustersController {
    
    /**
     * The scheduler controller
     */
    @Autowired
    private SchedulerController schedulerController;
    
    /**
     * Get all clusters
     * 
     * @return Returns all workers
     */
    @GetMapping(path = "")
    public ResponseEntity<?> get(){
        return ResponseEntity.ok(this.schedulerController.getAllClusters());
    }
    
    /**
     * Get cluster by id
     * 
     * @param id The code of cluster
     * @return Returns cluster
     */
    @GetMapping(path = "{id}")
    public ResponseEntity<?> getSingle(@PathVariable String id){
        return ResponseEntity.of(this.schedulerController.getClusterById(id));
    }
    
    /**
     * Create and store worker instance
     * 
     * @param worker The worker to register
     * @return Returns saved worker
     */
    @PostMapping(path = "")
    public ResponseEntity<?> joinOne(@RequestBody WorkerJoinInput worker){
        return ResponseEntity.ok(this.schedulerController.joinWorker(worker));
    }
    
    /**
     * Update worker heartbeat
     * 
     * @param heartbeat The heartbeat to update
     * @return Returns saved worker
     */
    @PutMapping(path = "")
    public ResponseEntity<?> putHealth(@RequestBody WorkerHeartbeat heartbeat){
        return ResponseEntity.ok(this.schedulerController.updateWorker(heartbeat));
    }
    
    /**
     * Delete worker by id
     * 
     * @param id The id of worker to delete
     * @return Returns deleted agent if available
     */
    @DeleteMapping(path = "{id}")
    public ResponseEntity<?> deleteClusterById(@PathVariable String id){
        return ResponseEntity.of(this.schedulerController.deleteClusterById(id));
    }
    
    /**
     * Delete all clusters
     * 
     * @return Returns number of deleted
     */
    @DeleteMapping(path = "")
    public ResponseEntity<?> deleteWorkers(){
        return ResponseEntity.ok(this.schedulerController.deleteAllClusters());
    }
}
