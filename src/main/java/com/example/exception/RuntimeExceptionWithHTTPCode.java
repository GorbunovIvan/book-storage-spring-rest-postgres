package com.example.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;

@RequiredArgsConstructor
@Getter
public class RuntimeExceptionWithHTTPCode extends RuntimeException {

    private final HttpStatusCode httpStatusCode;

    public RuntimeExceptionWithHTTPCode(String message, HttpStatusCode httpStatusCode) {
        super(message);
        this.httpStatusCode = httpStatusCode;
    }
}
