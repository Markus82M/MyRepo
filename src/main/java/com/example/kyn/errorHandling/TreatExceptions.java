package com.example.kyn.errorHandling;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@Slf4j
public class TreatExceptions extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value
            = {IllegalStateException.class})
    protected ResponseEntity<Object> handleConflict(
            RuntimeException ex, WebRequest request) {
        String bodyOfResponse = "This should be application specific. More details:" + ex.getMessage();
        log.info("Exception occured:", ex);
        return handleExceptionInternal(ex, bodyOfResponse,
                new HttpHeaders(), HttpStatus.CONFLICT, request);
    }
}
