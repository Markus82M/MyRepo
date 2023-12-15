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
public class TreatSpecificExceptions {

    @ExceptionHandler(value = { IllegalArgumentException.class})
    protected ResponseEntity<Object> handleConflict(
            RuntimeException ex, WebRequest request) {
        String bodyOfResponse = "Missing some fields. More details:" + ex.getMessage();
        log.info("Exception occured:", ex);
        return ResponseEntity.badRequest().body(bodyOfResponse);
    }
}
