package io.imast.work4j.data.impl;

import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import com.mongodb.TransactionOptions;
import com.mongodb.WriteConcern;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.TransactionBody;
import java.util.function.Function;

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
     * @param client The mongo client 
     * @param options The transaction options
     * @param function The function to perform
     * @return Returns result done in transaction
     */
    public static <T> T withTransaction(MongoClient client, TransactionOptions options, Function<ClientSession, T> function){
        
        // starts new client session within client
        var session = client.startSession();
                
        // new instance to transaction body
        TransactionBody<T> body = () -> function.apply(session);
        
        // do in try block to close anyways (same as finally)
        try (session) {
            
            // do withing transaction
            return session.withTransaction(body, options);
        }
    }
    
    /**
     * Do the operation within the given client session
     * 
     * @param <T> The output type of operation
     * @param client The mongo client 
     * @param function The function to perform
     * @return Returns result done in transaction
     */
    public static <T> T withTransaction(MongoClient client, Function<ClientSession, T> function){
        
        // starts new client session within client
        var session = client.startSession();
                
        // new instance to transaction body
        TransactionBody<T> body = () -> function.apply(session);
        
        // do in try block to close anyways (same as finally)
        try (session) {
            
            // do withing transaction
            return session.withTransaction(body, defaultTransactionOptions());
        }
    }
}
