package io.imast.work4j.data;

import io.imast.work4j.model.agent.AgentDefinition;
import java.util.List;
import java.util.Optional;

/**
 * The agent definition repository
 * 
 * @author davitp
 */
public interface AgentDefinitionRepository {
    
    /**
     * A special procedure to prepare schema
     * 
     * @return Returns if schema is ready
     */
    public boolean prepare();
    
    /**
     * Gets the agent definition by identifier
     * 
     * @param id The agent definition id
     * @return Returns agent definition if found
     */
    public Optional<AgentDefinition> getById(String id);
    
    /**
     * Gets all the agent definitions
     * 
     * @return Returns set of all job iterations 
     */
    public List<AgentDefinition> getAll();
    
    /**
     * Updates (inserts or updates) a agent definition into the data store
     * 
     * @param agentDefinition The agent definition to save
     * @return Returns saved agent definition
     */
    public Optional<AgentDefinition> update(AgentDefinition agentDefinition);
    
    /**
     * Inserts a agent definition into the data store
     * 
     * @param agentDefinition The agent definition to save
     * @return Returns saved agent definition
     */
    public Optional<AgentDefinition> insert(AgentDefinition agentDefinition);
    
    /**
     * Deletes an entry by id and returns deleted one
     * 
     * @param id The id of agent definition to delete
     * @return Returns deleted agent definition item
     */
    public Optional<AgentDefinition> deleteById(String id);
    
    /**
     * Deletes all the records
     * 
     * @return Returns number of deleted records
     */
    public long deleteAll();
}
