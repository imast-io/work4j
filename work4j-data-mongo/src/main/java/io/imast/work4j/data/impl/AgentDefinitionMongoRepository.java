package io.imast.work4j.data.impl;

import com.mongodb.client.MongoDatabase;
import io.imast.core.mongo.BaseMongoRepository;
import io.imast.core.mongo.SimplePojoRegistries;
import io.imast.core.mongo.StringIdGenerator;
import io.imast.work4j.data.AgentDefinitionRepository;
import io.imast.work4j.model.agent.AgentDefinition;
import java.util.Optional;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.ClassModel;

/**
 * The agent definition repository
 * 
 * @author davitp
 */
public class AgentDefinitionMongoRepository extends BaseMongoRepository<String, AgentDefinition> implements AgentDefinitionRepository{

    /**
     * Creates new instance of agent definitions mongo repository
     * 
     * @param mongoDatabase The underlying mongo database
     */
    public AgentDefinitionMongoRepository(MongoDatabase mongoDatabase){
        super(mongoDatabase, "work4j_agent_definitions", AgentDefinition.class);
    }
    
    /**
     * The custom Codec registry
     * 
     * @return Returns custom Codec registry
     */
    @Override
    protected CodecRegistry customizer(){
        return SimplePojoRegistries.simple(
            ClassModel.builder(this.clazz)
                    .idGenerator(new StringIdGenerator())
                    .build()
        );
    }
    
    /**
     * Updates (inserts or updates) a agent definition into the data store
     * 
     * @param agentDefinition The agent definition to save
     * @return Returns saved agent definition
     */
    @Override
    public Optional<AgentDefinition> update(AgentDefinition agentDefinition) {
        return this.upsert(agentDefinition, a -> a.getId());
    }   

    /**
     * A special procedure to prepare schema
     * 
     * @return Returns if schema is ready
     */
    @Override
    public boolean prepare() {
        return true;
    }
}
