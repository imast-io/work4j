package io.imast.samples.scheduler.handle;

import io.imast.work4j.data.exception.SchedulerDataException;
import io.imast.work4j.model.issue.DataIssue;
import io.imast.work4j.model.issue.DataIssues;
import io.imast.work4j.model.issue.IssueSeverity;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

/**
 * The exception handling module for scheduler data errors
 * 
 * @author davitp
 */
@ControllerAdvice
public class SchedulerDataExecutionHandler {
  
    /**
     * Handle scheduler data exception and respond with error entity
     * 
     * @param ex The exception to handle
     * @param request The web request 
     * @return Returns the error response entity
     */
    @ExceptionHandler(value = {SchedulerDataException.class})
    public ResponseEntity<DataIssues> resourceNotFoundException(SchedulerDataException ex, WebRequest request) {

        // get the error messages
        var messages = ex.getErrors() == null ? Arrays.asList("Unknown Error") : ex.getErrors();
        
        // build the set of issues
        var issues = messages
                .stream()
                .map(msg -> DataIssue.builder().message(msg).severity(IssueSeverity.ERROR).build())
                .collect(Collectors.toList());

      return new ResponseEntity<>(new DataIssues(issues), HttpStatus.BAD_REQUEST);
    }
}