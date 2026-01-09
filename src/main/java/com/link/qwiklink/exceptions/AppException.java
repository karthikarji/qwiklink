package com.link.qwiklink.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AppException extends RuntimeException {

    private final HttpStatus status;
    private final ErrorType type;

    /**
     * Creates an exception with INTERNAL error type by default.
     */
    public AppException(HttpStatus status, String message) {
        super(message);
        this.status = status;
        this.type = ErrorType.INTERNAL;
    }

    /**
     * Creates an exception with explicit error type.
     */
    public AppException(HttpStatus status, ErrorType type, String message) {
        super(message);
        this.status = status;
        this.type = type;
    }

    /**
     * Creates an exception with explicit error type and root cause.
     */
    public AppException(HttpStatus status, ErrorType type, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.type = type;
    }
}
