package io.imast.work4j.data.impl;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import com.mongodb.TransactionOptions;
import com.mongodb.WriteConcern;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.TransactionBody;
import java.util.function.Function;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import org.bson.codecs.pojo.PojoCodecProvider;

/**
 * Mongo operations shortcuts
 * 
 * @author davitp
 */
public class MongoOps {
    
    /**
     * The default transaction options
     * 
     * @return Returns the default transaction options
     */
    public static TransactionOptions defaultTransactionOptions(){
        return TransactionOptions.builder()
                .readPreference(ReadPreference.primary())
                .readConcern(ReadConcern.LOCAL)
                .writeConcern(WriteConcern.MAJORITY)
                .build();
    }
        
    /**
     * Do the operation within the given client session
     * 
     * @param <T> The output type of operation
     * @param transactional If execution should be transactional
     * @param client The mongo client 
     * @param function The function to perform
     * @return Returns result done in transaction
     */
    public static <T> T withinSession(boolean transactional, MongoClient client, Function<ClientSession, T> function){
        
        // starts new client session within client
        var session = client.startSession();
        
        // do in try block to close anyways (same as finally)
        try (session) {
            
            // do within session but without transactions
            if(!transactional){
                return function.apply(session);
            }
            
            // new instance to transaction body
            TransactionBody<T> body = () -> function.apply(session);
            
            // do withing transaction
            return session.withTransaction(body, defaultTransactionOptions());
        }
    }
    
    /**
     * Use the collection along with POJO Codec registry
     * 
     * @param <T> The type of document
     * @param collection The collection reference
     * @return Returns collection with new Codec registry
     */
    public static <T> MongoCollection<T> withPojo(MongoCollection<T> collection){
        return collection.withCodecRegistry(fromRegistries(getDefaultCodecRegistry(),
                fromProviders(PojoCodecProvider.builder().automatic(true).build())));
    }
}
