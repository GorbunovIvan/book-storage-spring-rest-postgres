package com.example.controller.controllerAdvice;

import com.example.exception.RuntimeExceptionWithHTTPCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeExceptionWithHTTPCode.class)
    public ResponseEntity<String> handleRuntimeExceptionWithHTTPCode(RuntimeExceptionWithHTTPCode e) {
        return new ResponseEntity<>(e.getMessage(), e.getHttpStatusCode());
    }
}
