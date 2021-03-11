package io.imast.samples.scheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

/**
 * The schedule management application
 * 
 * @author davitp
 */
@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class SchedulerApplication {

    /**
     * The entry point for "Controller Application"
     * 
     * @param args The arguments
     */
    public static void main(String[] args) {
        // create an application
        SpringApplication.run(SchedulerApplication.class, args);
    }
}
