package io.imast.work4j.worker;

/**
 * The persistence type
 * 
 * @author davitp
 */
public enum PersistenceType {
    
    /**
     * No persistence
     */
    NO,
    
    /**
     * The MySQL-based persistence
     */
    MYSQL,
    
    /**
     * The PostgresSQL-based persistence
     */
    POSTGRES,
    
    /**
     * The MSSQL-based persistence
     */
    MSSQL,
    
    /**
     * The Oracle-based persistence
     */
    ORACLE,
    
    /**
     * The WebLogic-based persistence
     */
    WEBLOGIC
}
