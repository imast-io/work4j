package io.imast.work4j.model;

/**
 * The tenants constants
 * 
 * @author davitp
 */
public class Tenants {
   
    /**
     * The value of default tenant
     */
    public static final String DEFAULT = "DEFAULT";
    
    /**
     * Use given tenant or default
     * 
     * @param tenant The tenant name
     * @return Returns given tenant or default
     */
    public static String maybe(String tenant){
        return (tenant == null || tenant.isBlank()) ? DEFAULT : tenant;
    }
}
