package io.imast.samples.scheduler.worker;

import lombok.extern.slf4j.Slf4j;

/**
 * A module to inject into job. It will just print given message on the screen. 
 * 
 * @author davitp
 */
@Slf4j
public class PrinterModule {
   
    /**
     * Print the given message on screen
     * 
     * @param message The message to print
     */
    public void print(String message){
        log.info(message);
    }
}
